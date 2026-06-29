<script setup>
import { ref, onMounted } from 'vue'
import { institutionApi } from '@/api/index.js'
import { ElMessage, ElMessageBox } from 'element-plus'

// ==================== 数据 ====================
const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})
const levelOptions = ['985', '211', '双一流', '普通本科', '专科']

// ==================== 加载列表 ====================
async function loadList() {
  const res = await institutionApi.list()
  list.value = Array.isArray(res) ? res : (res.data || [])
}
onMounted(loadList)

// ==================== 新增 / 编辑 ====================
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
    await institutionApi.update(form.value)
    ElMessage.success('修改成功')
  } else {
    await institutionApi.add(form.value)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  loadList()
}

// ==================== 删除 ====================
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该院校？', '提示', { type: 'warning' })
  await institutionApi.delete(row.id)
  ElMessage.success('删除成功')
  loadList()
}

// ==================== 管控操作 ====================
async function toggleOnline(row) {
  const newVal = row.isOnline === 1 ? 0 : 1
  await institutionApi.onlineStatus(row.id, newVal)
  ElMessage.success(newVal === 1 ? '已上线' : '已下线')
  loadList()
}
async function changeLevel(row) {
  const { value } = await ElMessageBox.prompt('输入新的 level', '改 985/211 属性', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: row.level,
  })
  if (value) {
    await institutionApi.updateLevel(row.id, value)
    ElMessage.success('修改成功')
    loadList()
  }
}
async function forceStatus(row) {
  const { value } = await ElMessageBox.prompt('输入审核状态（0=待审 1=通过 2=驳回）', '强制改审核状态', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputValue: String(row.status),
  })
  if (value !== null && value !== undefined) {
    await institutionApi.updateStatus(row.id, Number(value))
    ElMessage.success('状态已修改')
    loadList()
  }
}
</script>

<template>
  <div class="page">
    <h2>🏫 院校库管理</h2>
    <el-button type="primary" @click="openAdd">新增院校</el-button>

    <el-table :data="list" stripe border style="margin-top:12px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="institutionCode" label="院校代码" width="100" />
      <el-table-column prop="institutionName" label="院校名称" width="160" />
      <el-table-column prop="level" label="985/211" width="100" />
      <el-table-column prop="nature" label="公办/民办" width="90">
        <template #default="{ row }">{{ row.nature === 1 ? '公办' : '民办' }}</template>
      </el-table-column>
      <el-table-column prop="provinceCode" label="省份" width="80" />
      <el-table-column prop="contactPhone" label="联系电话" width="120" />
      <el-table-column label="上线" width="70">
        <template #default="{ row }">
          <el-switch :model-value="row.isOnline === 1" @click="toggleOnline(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="status" label="审核状态" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.status === 1" type="success">通过</el-tag>
          <el-tag v-else-if="row.status === 2" type="danger">驳回</el-tag>
          <el-tag v-else type="info">待审</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          <el-button size="small" @click="changeLevel(row)">改属性</el-button>
          <el-button size="small" type="warning" @click="forceStatus(row)">改状态</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑院校' : '新增院校'" width="600px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="院校代码">
          <el-input v-model="form.institutionCode" />
        </el-form-item>
        <el-form-item label="院校名称">
          <el-input v-model="form.institutionName" />
        </el-form-item>
        <el-form-item label="985/211">
          <el-select v-model="form.level">
            <el-option v-for="lv in levelOptions" :key="lv" :label="lv" :value="lv" />
          </el-select>
        </el-form-item>
        <el-form-item label="公办/民办">
          <el-select v-model="form.nature">
            <el-option label="公办" :value="1" />
            <el-option label="民办" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="省份代码">
          <el-input v-model="form.provinceCode" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.contactEmail" />
        </el-form-item>
        <el-form-item label="官网">
          <el-input v-model="form.officialWebsite" />
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" />
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
