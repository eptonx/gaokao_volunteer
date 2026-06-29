<script setup>
import { ref, onMounted } from 'vue'
import { admissionHistoryApi } from '@/api/index.js'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})
const uploading = ref(false)

async function loadList() {
  const res = await admissionHistoryApi.selectAll()
  list.value = Array.isArray(res) ? res : (res.data || [])
}
onMounted(loadList)

// ==================== 增删改 ====================
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
    await admissionHistoryApi.update(form.value)
    ElMessage.success('修改成功')
  } else {
    await admissionHistoryApi.add(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadList()
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该记录？', '提示', { type: 'warning' })
  await admissionHistoryApi.delete(row.id)
  ElMessage.success('删除成功')
  loadList()
}

// ==================== 发布 ====================
async function togglePublish(row) {
  const newVal = row.status === 1 ? 0 : 1
  await admissionHistoryApi.publish(row.id, newVal)
  ElMessage.success(newVal === 1 ? '已发布' : '已取消发布')
  loadList()
}

// ==================== Excel ====================
async function handleImport(file) {
  uploading.value = true
  try {
    await admissionHistoryApi.import(file)
    ElMessage.success('导入成功')
    loadList()
  } catch {
    ElMessage.error('导入失败')
  } finally {
    uploading.value = false
  }
}
function downloadTemplate() {
  admissionHistoryApi.downloadTemplate().then((res) => {
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const a = document.createElement('a')
    a.href = url
    a.download = '历年录取数据导入模板.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
  })
}
</script>

<template>
  <div class="page">
    <h2>📊 历年录取数据管理</h2>
    <div style="display:flex; gap:8px; align-items:center; flex-wrap:wrap">
      <el-button type="primary" @click="openAdd">新增记录</el-button>
      <el-button @click="downloadTemplate">📥 下载 Excel 模板</el-button>
      <el-upload
        :show-file-list="false"
        accept=".xlsx,.xls"
        :http-request="({ file }) => handleImport(file)"
      >
        <el-button type="success" :loading="uploading">📤 导入 Excel</el-button>
      </el-upload>
    </div>

    <el-table :data="list" stripe border style="margin-top:12px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="institutionId" label="院校ID" width="80" />
      <el-table-column prop="majorId" label="专业ID" width="80" />
      <el-table-column prop="majorName" label="专业名称" width="140" />
      <el-table-column prop="year" label="年份" width="70" />
      <el-table-column prop="provinceCode" label="省份" width="80" />
      <el-table-column prop="categoryCode" label="科类" width="80" />
      <el-table-column prop="minScore" label="最低分" width="80" />
      <el-table-column prop="maxScore" label="最高分" width="80" />
      <el-table-column prop="avgScore" label="平均分" width="80" />
      <el-table-column prop="minRank" label="最低位次" width="90" />
      <el-table-column prop="enrollmentCount" label="录取人数" width="90" />
      <el-table-column label="发布状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已发布' : '未发布' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="togglePublish(row)">
            {{ row.status === 1 ? '取消发布' : '发布' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑录取数据' : '新增录取数据'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="院校ID">
          <el-input v-model="form.institutionId" />
        </el-form-item>
        <el-form-item label="专业ID">
          <el-input v-model="form.majorId" />
        </el-form-item>
        <el-form-item label="专业名称">
          <el-input v-model="form.majorName" />
        </el-form-item>
        <el-form-item label="年份">
          <el-input-number v-model="form.year" :min="2000" :max="2030" />
        </el-form-item>
        <el-form-item label="省份代码">
          <el-input v-model="form.provinceCode" />
        </el-form-item>
        <el-form-item label="科类代码">
          <el-input v-model="form.categoryCode" />
        </el-form-item>
        <el-form-item label="最低分">
          <el-input-number v-model="form.minScore" />
        </el-form-item>
        <el-form-item label="最高分">
          <el-input-number v-model="form.maxScore" />
        </el-form-item>
        <el-form-item label="平均分">
          <el-input-number v-model="form.avgScore" />
        </el-form-item>
        <el-form-item label="最低位次">
          <el-input-number v-model="form.minRank" />
        </el-form-item>
        <el-form-item label="录取人数">
          <el-input-number v-model="form.enrollmentCount" />
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
