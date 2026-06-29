@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion
title 高考志愿系统 - 一键启动

:: ==================== 配置 ====================
set MYSQL_USER=dev_team
set MYSQL_PASS=dev_team_2026
set MYSQL_DB=gaokao_volunteer
set SQL_FILE=gaokao_volunteer.sql

set BACKEND_DIR=backend
set FRONTEND_DIR=frontend\vuezhiyuan-project

set BACKEND_PORT=8080
set FRONTEND_PORT=5173

set SCRIPT_DIR=%~dp0
set SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

:: ==================== 环境检查 ====================
echo.
echo ==============================================
echo   1. 环境检查
echo ==============================================

call :check java  "Java JDK 21+"  >nul 2>&1 || (
    echo [✗] Java 未安装，请先安装 JDK 21+
    pause
    exit /b 1
)
echo [✓] Java 已就绪

call :check node   "Node.js"  >nul 2>&1 || (
    echo [✗] Node.js 未安装，请先安装
    pause
    exit /b 1
)
echo [✓] Node.js 已就绪

call :check npm    "npm" >nul 2>&1 || (
    echo [✗] npm 未安装
    pause
    exit /b 1
)
echo [✓] npm 已就绪

call :check mysql  "MySQL" >nul 2>&1 || (
    echo [✗] MySQL 未安装或不在 PATH 中
    pause
    exit /b 1
)
echo [✓] MySQL 已就绪

:: ==================== 数据库初始化 ====================
echo.
echo ==============================================
echo   2. 数据库初始化
echo ==============================================

echo 检测 MySQL 连接...
mysql -u%MYSQL_USER% -p%MYSQL_PASS% -e "SELECT 1" 2>nul
if !errorlevel! neq 0 (
    echo [✗] 无法连接 MySQL，请检查用户名密码或 MySQL 服务是否启动
    pause
    exit /b 1
)
echo [✓] MySQL 连接成功

echo 创建/确认数据库: %MYSQL_DB% ...
mysql -u%MYSQL_USER% -p%MYSQL_PASS% -e "CREATE DATABASE IF NOT EXISTS %MYSQL_DB% DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;" 2>nul
echo [✓] 数据库 %MYSQL_DB% 已就绪

echo 导入 SQL 文件: %SQL_FILE% ...
mysql -u%MYSQL_USER% -p%MYSQL_PASS% %MYSQL_DB% < "%SCRIPT_DIR%\%SQL_FILE%" 2>nul
if !errorlevel! neq 0 (
    echo [✗] SQL 导入失败
    pause
    exit /b 1
)
echo [✓] 数据表导入完成

:: ==================== 安装前端依赖 ====================
echo.
echo ==============================================
echo   3. 安装依赖
echo ==============================================

if exist "%SCRIPT_DIR%\%FRONTEND_DIR%\node_modules" (
    echo [✓] 前端依赖已存在，跳过 npm install
) else (
    echo 正在安装前端依赖，请稍候...
    cd /d "%SCRIPT_DIR%\%FRONTEND_DIR%"
    call npm install
    cd /d "%SCRIPT_DIR%"
    echo [✓] 前端依赖安装完成
)

:: ==================== 启动服务 ====================
echo.
echo ==============================================
echo   4. 启动服务
echo ==============================================

echo 启动后端 (Spring Boot, 端口 %BACKEND_PORT%)...
start "高考志愿-后端" cmd /c "cd /d "%SCRIPT_DIR%\%BACKEND_DIR%" && mvnw spring-boot:run"

echo 启动前端 (Vite, 端口 %FRONTEND_PORT%)...
start "高考志愿-前端" cmd /c "cd /d "%SCRIPT_DIR%\%FRONTEND_DIR%" && npm run dev"

:: ==================== 等待 & 打开浏览器 ====================
echo.
echo ==============================================
echo   5. 等待服务就绪 & 打开浏览器
echo ==============================================

echo 等待后端启动...
set BACKEND_OK=0
for /L %%i in (1,1,30) do (
    curl -s http://localhost:%BACKEND_PORT% >nul 2>&1
    if !errorlevel! equ 0 (
        set BACKEND_OK=1
        goto :backend_ready
    )
    timeout /t 2 /nobreak >nul
)
:backend_ready
if !BACKEND_OK! equ 1 (
    echo [✓] 后端已启动 (http://localhost:%BACKEND_PORT%)
) else (
    echo [!] 后端启动中，请耐心等待...
)

echo 等待前端启动...
set FRONTEND_OK=0
for /L %%i in (1,1,15) do (
    curl -s http://localhost:%FRONTEND_PORT% >nul 2>&1
    if !errorlevel! equ 0 (
        set FRONTEND_OK=1
        goto :frontend_ready
    )
    timeout /t 1 /nobreak >nul
)
:frontend_ready
if !FRONTEND_OK! equ 1 (
    echo [✓] 前端已启动 (http://localhost:%FRONTEND_PORT%)
) else (
    echo [!] 前端启动中，请耐心等待...
)

:: 打开浏览器
start "" "http://localhost:%FRONTEND_PORT%" 2>nul

echo.
echo ==============================================
echo   高考志愿系统 启动完成!
echo   前端地址: http://localhost:%FRONTEND_PORT%
echo   后端地址: http://localhost:%BACKEND_PORT%
echo   按任意键退出此窗口 (服务不受影响)
echo ==============================================
pause >nul
exit /b 0

:: ==================== 工具函数 ====================
:check
where %1 >nul 2>&1
exit /b %errorlevel%
