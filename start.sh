#!/bin/bash
# ============================================
#  高考志愿系统 - 一键启动脚本 (Git Bash / Linux / macOS)
#  用法: bash start.sh
# ============================================

set -e

# ==================== 配置 ====================
MYSQL_USER="dev_team"
MYSQL_PASS="dev_team_2026"
MYSQL_DB="gaokao_volunteer"
SQL_FILE="gaokao_volunteer.sql"

BACKEND_DIR="backend"
FRONTEND_DIR="frontend/vuezhiyuan-project"

BACKEND_PORT=8080
FRONTEND_PORT=5173

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# JDK 21 候选路径 (按顺序查找)
JDK21_CANDIDATES=(
    "/e/jdk21"
    "/c/Program Files/Java/latest/jdk-21"
    "/e/Openjdk/jdk-21"
)

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

info()  { echo -e "${GREEN}[✓]${NC} $1"; }
warn()  { echo -e "${YELLOW}[!]${NC} $1"; }
error() { echo -e "${RED}[✗]${NC} $1"; }
title() { echo -e "\n${CYAN}>>> $1${NC}\n"; }

# ==================== 环境检查 ====================
title "1. 环境检查"

check_cmd() {
    if ! command -v "$1" &> /dev/null; then
        error "$2 未安装，请先安装后再运行"
        exit 1
    fi
    info "$2 已就绪 ($(command -v "$1"))"
}

check_cmd java    "Java JDK 21+"
check_cmd node    "Node.js"
check_cmd npm     "npm"
check_cmd mysql   "MySQL Client"

# 自动检测 JDK 21+
JAVA_VER=$(java -version 2>&1 | head -1 | grep -oP '\d+\.\d+\.\d+' | cut -d. -f1)
JDK21_HOME=""

# 优先在候选路径中查找 JDK 21
for candidate in "${JDK21_CANDIDATES[@]}"; do
    if [ -f "$candidate/bin/java" ]; then
        JDK21_HOME="$candidate"
        info "Java 21 已就绪 ($candidate)"
        break
    fi
done

# 候选都没找到，检查 PATH 中的 Java 版本
if [ -z "$JDK21_HOME" ]; then
    if [ "$JAVA_VER" -ge 21 ]; then
        info "Java $JAVA_VER 在 PATH 中已就绪"
    else
        error "未找到 JDK 21 (PATH 版本为 $JAVA_VER)，请安装或修改 JDK21_CANDIDATES"
        exit 1
    fi
fi

# ==================== 数据库初始化 ====================
title "2. 数据库初始化"

echo "检测 MySQL 连接..."
if ! mysql -u"$MYSQL_USER" -p"$MYSQL_PASS" -e "SELECT 1" &>/dev/null; then
    error "无法连接 MySQL，请检查用户名密码或 MySQL 服务是否启动"
    exit 1
fi
info "MySQL 连接成功"

echo "创建/确认数据库: $MYSQL_DB ..."
mysql -u"$MYSQL_USER" -p"$MYSQL_PASS" -e "CREATE DATABASE IF NOT EXISTS $MYSQL_DB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;" 2>/dev/null
info "数据库 $MYSQL_DB 已就绪"

echo "导入 SQL 文件: $SQL_FILE ..."
mysql -u"$MYSQL_USER" -p"$MYSQL_PASS" "$MYSQL_DB" < "$SQL_FILE" 2>/dev/null
info "数据表导入完成"

# ==================== 安装依赖 ====================
title "3. 安装依赖"

# 前端依赖
if [ -d "$FRONTEND_DIR/node_modules" ]; then
    info "前端依赖已存在，跳过 npm install"
else
    echo "安装前端依赖..."
    cd "$FRONTEND_DIR"
    npm install
    cd "$SCRIPT_DIR"
    info "前端依赖安装完成"
fi

# ==================== 启动服务 ====================
title "4. 启动服务"

# 清理函数：退出时杀掉子进程
cleanup() {
    echo ""
    warn "正在停止所有服务..."
    [ -n "$BACKEND_PID" ] && kill "$BACKEND_PID" 2>/dev/null
    [ -n "$FRONTEND_PID" ] && kill "$FRONTEND_PID" 2>/dev/null
    info "服务已停止"
    exit 0
}
trap cleanup SIGINT SIGTERM

# 启动后端
echo "启动后端 (Spring Boot, 端口 $BACKEND_PORT)..."
if [ -n "$JDK21_HOME" ]; then
    export JAVA_HOME="$JDK21_HOME"
    echo "  JAVA_HOME=$JAVA_HOME"
fi
cd "$SCRIPT_DIR/$BACKEND_DIR"
if [ -f "./mvnw" ]; then
    ./mvnw spring-boot:run -q &
else
    mvn spring-boot:run -q &
fi
BACKEND_PID=$!
cd "$SCRIPT_DIR"

# 启动前端
echo "启动前端 (Vite, 端口 $FRONTEND_PORT)..."
cd "$FRONTEND_DIR"
npm run dev &
FRONTEND_PID=$!
cd "$SCRIPT_DIR"

# ==================== 等待就绪 ====================
title "5. 等待服务就绪"

echo "等待后端启动..."
for i in $(seq 1 60); do
    if curl -s http://localhost:$BACKEND_PORT &>/dev/null; then
        info "后端已启动 (http://localhost:$BACKEND_PORT)"
        break
    fi
    if [ "$i" -eq 60 ]; then
        warn "后端启动超时，请手动检查"
    fi
    sleep 2
done

echo "等待前端启动..."
for i in $(seq 1 30); do
    if curl -s http://localhost:$FRONTEND_PORT &>/dev/null; then
        info "前端已启动 (http://localhost:$FRONTEND_PORT)"
        break
    fi
    if [ "$i" -eq 30 ]; then
        warn "前端启动超时，请手动检查"
    fi
    sleep 1
done

# ==================== 打开浏览器 ====================
echo ""
echo -e "${GREEN}=============================================="
echo "  高考志愿系统 启动完成!"
echo "  前端地址: http://localhost:$FRONTEND_PORT"
echo "  后端地址: http://localhost:$BACKEND_PORT"
echo "  按 Ctrl+C 停止所有服务"
echo -e "==============================================${NC}"

# 尝试打开浏览器
if command -v start &>/dev/null; then
    start "http://localhost:$FRONTEND_PORT" 2>/dev/null || true
elif command -v open &>/dev/null; then
    open "http://localhost:$FRONTEND_PORT" 2>/dev/null || true
elif command -v xdg-open &>/dev/null; then
    xdg-open "http://localhost:$FRONTEND_PORT" 2>/dev/null || true
fi

# 等待子进程
wait
