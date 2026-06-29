<script setup>
import { ref, onMounted } from 'vue'
import { majorDictApi } from '@/api/index.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})
const searchName = ref('')

async function loadList() {
  const res = await majorDictApi.list(searchName.value || '')
  list.value = Array.isArray(res) ? res : (res.data || [])
}
onMounted(loadList)

function openAdd() {
  isEdit.value = false
  form.value = {}
  dialogVisible.value = true
}
function openEdit(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}
async function handleSave() {
  if (isEdit.value) {
    await majorDictApi.update(form.value)
    ElMessage.success('修改成功')
  } else {
    await majorDictApi.save(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadList()
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该专业？', '提示', { type: 'warning' })
  await majorDictApi.delete(row.id)
  ElMessage.success('删除成功')
  loadList()
}
async function toggleOnline(row) {
  const newVal = row.status === 1 ? 0 : 1
  await majorDictApi.onlineStatus(row.id, newVal)
  ElMessage.success(newVal === 1 ? '已上线' : '已下线')
  loadList()
}
</script>

<template>
  <div class="page">
    <h2>📚 专业库管理</h2>
    <div style="display:flex; gap:8px; align-items:center">
      <el-input v-model="searchName" placeholder="搜索专业名称" style="width:200px" clearable @clear="loadList" />
      <el-button type="primary" @click="loadList">搜索</el-button>
      <el-button type="success" @click="openAdd">新增专业</el-button>
    </div>

    <el-table :data="list" stripe border style="margin-top:12px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="code" label="代码" width="100" />
      <el-table-column prop="name" label="名称" width="160" />
      <el-table-column prop="category" label="大类" width="120" />
      <el-table-column prop="subCategory" label="子类" width="120" />
      <el-table-column prop="description" label="描述" min-width="180" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @click="toggleOnline(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑专业' : '新增专业'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="代码">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="大类">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="子类">
          <el-input v-model="form.subCategory" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 16px; }
h2 { margin-bottom: 12px; }
</style>
