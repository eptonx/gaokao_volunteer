<template>
  <div class="admin-page">
    <!-- 顶部操作栏 -->
    <div style="margin-bottom:20px; display:flex; gap:10px; flex-wrap:wrap; align-items:center">
      <el-button type="warning" @click="openPwdDialog">修改密码</el-button>
      <el-button type="danger" @click="logout">退出登录</el-button>
      <el-input v-model="searchKeyword" placeholder="搜索账号/姓名" clearable style="width:200px" @keyup.enter="searchAdmin"></el-input>
      <el-button type="primary" @click="searchAdmin">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button type="success" @click="openAdd">新增管理员</el-button>
    </div>

    <!-- 管理员表格 -->
    <el-table :data="adminList" border @row-click="selectAdmin" highlight-current-row>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="username" label="账号" width="120"></el-table-column>
      <el-table-column prop="realName" label="姓名" width="120"></el-table-column>
      <el-table-column prop="roleId" label="角色ID" width="80"></el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="scope">
          {{ scope.row.status === 1 ? '正常' : '禁用' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="260">
        <template #default="scope">
          <el-button size="small" type="primary" @click.stop="openEdit(scope.row)">编辑</el-button>
          <el-button size="small" :type="scope.row.status===1?'danger':'success'" @click.stop="changeStatus(scope.row)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-popconfirm title="确定删除该管理员？" @confirm="deleteAdmin(scope.row.id)">
            <template #reference>
              <el-button size="small" type="danger" @click.stop>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      @current-change="getAdminList"
      @size-change="getAdminList"
      style="margin-top:20px"
    />

    <!-- 角色分配区 -->
    <div style="margin-top:20px">
      <el-select v-model="selectRoleId" placeholder="选择分配角色">
        <el-option v-for="r in roleList" :key="r.id" :label="r.name" :value="r.id"></el-option>
      </el-select>
      <el-button @click="assignRole" :disabled="!selectedAdminId">分配角色</el-button>
    </div>

    <!-- 角色管理表格 -->
    <h3 style="margin-top:30px">角色列表</h3>
    <el-table :data="roleList" border style="margin-top:10px">
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="name" label="角色名称" width="120"></el-table-column>
      <el-table-column prop="code" label="角色编码" width="140"></el-table-column>
      <el-table-column prop="description" label="描述"></el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="scope">
          {{ scope.row.status === 1 ? '正常' : '禁用' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button size="small" :type="scope.row.status===1?'danger':'success'" @click="changeRoleStatus(scope.row)">
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑管理员弹窗 -->
    <el-dialog v-model="formVisible" :title="isEdit ? '编辑管理员' : '新增管理员'">
      <el-form :model="adminForm" label-width="80px">
        <el-form-item label="账号">
          <el-input v-model="adminForm.username" placeholder="请输入账号"></el-input>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码">
          <el-input v-model="adminForm.passwordHash" placeholder="请输入密码" type="password"></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="adminForm.realName" placeholder="请输入真实姓名"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="adminForm.roleId" placeholder="选择角色">
            <el-option v-for="r in roleList" :key="r.id" :label="r.name" :value="r.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="adminForm.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认{{ isEdit ? '修改' : '新增' }}</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdVisible" title="修改密码">
      <el-input v-model="pwdForm.oldPwd" placeholder="原密码"></el-input>
      <el-input v-model="pwdForm.newPwd" placeholder="新密码" style="margin-top:10px"></el-input>
      <template #footer>
        <el-button @click="pwdVisible=false">取消</el-button>
        <el-button type="primary" @click="submitPwd">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdminPageApi, addAdminApi, updateAdminApi, deleteAdminApi, changeAdminStatusApi, assignRoleApi, getRoleListApi, updateRoleStatusApi, logoutApi, updatePwdApi } from '../assets/api/index'

const router = useRouter()
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const adminList = ref([])
const roleList = ref([])
const selectRoleId = ref('')
const selectedAdminId = ref(null)
const pwdVisible = ref(false)
const pwdForm = reactive({ oldPwd: '', newPwd: '' })

// 新增/编辑
const formVisible = ref(false)
const isEdit = ref(false)
const adminForm = reactive({
  id: null,
  username: '',
  passwordHash: '',
  realName: '',
  roleId: '',
  status: 1
})

// 页面加载
onMounted(() => {
  getAdminList()
  getRoleData()
})

// 获取管理员列表
const getAdminList = async () => {
  const keyword = searchKeyword.value || undefined
  const res = await getAdminPageApi(pageNum.value, pageSize.value, keyword)
  if (res && res.records) {
    adminList.value = res.records
    total.value = res.total
  } else {
    adminList.value = res || []
    if (adminList.value.length === pageSize.value) {
      total.value = pageNum.value * pageSize.value + 1
    } else {
      total.value = (pageNum.value - 1) * pageSize.value + adminList.value.length
    }
  }
}

// 搜索
const searchAdmin = () => {
  pageNum.value = 1
  getAdminList()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  pageNum.value = 1
  getAdminList()
}

// 选中管理员行
const selectAdmin = (row) => {
  selectedAdminId.value = row.id
}

// 获取角色
const getRoleData = async () => {
  roleList.value = await getRoleListApi()
}

// 打开新增弹窗
const openAdd = () => {
  isEdit.value = false
  adminForm.id = null
  adminForm.username = ''
  adminForm.passwordHash = ''
  adminForm.realName = ''
  adminForm.roleId = ''
  adminForm.status = 1
  formVisible.value = true
}

// 打开编辑弹窗
const openEdit = (row) => {
  isEdit.value = true
  adminForm.id = row.id
  adminForm.username = row.username
  adminForm.passwordHash = ''
  adminForm.realName = row.realName || ''
  adminForm.roleId = row.roleId || ''
  adminForm.status = row.status
  formVisible.value = true
}

// 提交新增/编辑
const submitForm = async () => {
  if (!adminForm.username) return ElMessage.warning('请输入账号')
  if (!isEdit.value && !adminForm.passwordHash) return ElMessage.warning('请输入密码')
  if (isEdit.value) {
    await updateAdminApi({
      id: adminForm.id,
      username: adminForm.username,
      passwordHash: '',  // 编辑时不改密码
      realName: adminForm.realName,
      roleId: adminForm.roleId,
      status: adminForm.status
    })
    ElMessage.success('修改成功')
  } else {
    await addAdminApi({
      username: adminForm.username,
      passwordHash: adminForm.passwordHash,
      realName: adminForm.realName,
      roleId: adminForm.roleId,
      status: adminForm.status
    })
    ElMessage.success('新增成功')
  }
  formVisible.value = false
  getAdminList()
}

// 删除管理员
const deleteAdmin = async (id) => {
  await deleteAdminApi(id)
  ElMessage.success('删除成功')
  getAdminList()
}

// 启用禁用管理员
const changeStatus = async (row) => {
  const s = row.status === 1 ? 0 : 1
  await changeAdminStatusApi(row.id, s)
  ElMessage.success('操作成功')
  getAdminList()
}

// 分配角色
const assignRole = async () => {
  if (!selectedAdminId.value) return ElMessage.warning('请先在表格中点击选择一个管理员')
  if (!selectRoleId.value) return ElMessage.warning('请选择一个角色')
  await assignRoleApi(selectedAdminId.value, selectRoleId.value)
  ElMessage.success('分配成功')
  getAdminList()
}

// 启用/禁用角色
const changeRoleStatus = async (row) => {
  const s = row.status === 1 ? 0 : 1
  await updateRoleStatusApi(row.id, s)
  ElMessage.success('操作成功')
  getRoleData()
}

// 退出登录
const logout = async () => {
  await logoutApi()
  ElMessage.success('已登出')
  router.push('/login')
}

// 打开改密码弹窗
const openPwdDialog = () => {
  pwdForm.oldPwd = ''
  pwdForm.newPwd = ''
  pwdVisible.value = true
}

// 提交密码修改
const submitPwd = async () => {
  await updatePwdApi(pwdForm)
  ElMessage.success('密码修改成功，请重新登录')
  pwdVisible.value = false
  router.push('/login')
}
</script>
