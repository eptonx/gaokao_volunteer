<template>
  <div>
    <el-tabs v-model="tab" @tab-change="onTabChange">
      <el-tab-pane label="待审核列表" name="wait"></el-tab-pane>
      <el-tab-pane label="审核历史" name="history"></el-tab-pane>
    </el-tabs>

    <!-- 搜索栏 -->
    <div style="margin-bottom:16px; display:flex; gap:10px; align-items:center">
      <el-input v-model="searchKeyword" placeholder="搜索文件名" clearable style="width:200px" @keyup.enter="searchList"></el-input>
      <el-input v-if="tab === 'wait'" v-model="searchInstId" placeholder="院校ID" clearable style="width:120px" @keyup.enter="searchList"></el-input>
      <el-button type="primary" @click="searchList">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="70"></el-table-column>
      <el-table-column prop="institutionName" label="院校名称" width="160">
        <template #default="scope">
          {{ scope.row.institutionName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="institutionId" label="院校ID" width="80"></el-table-column>
      <el-table-column prop="fileName" label="文件名" min-width="140" show-overflow-tooltip></el-table-column>
      <el-table-column prop="materialType" label="材料类型" width="100">
        <template #default="scope">
          {{ materialTypeMap[scope.row.materialType] || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="fileSize" label="文件大小" width="100">
        <template #default="scope">
          {{ formatFileSize(scope.row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="提交时间" width="160">
        <template #default="scope">
          {{ scope.row.createdAt || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.reviewStatus === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.reviewStatus === 1" type="success">已通过</el-tag>
          <el-tag v-else type="danger">已驳回</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="180" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openDetail(scope.row.id)">查看详情</el-button>
          <template v-if="scope.row.reviewStatus === 0">
            <el-button type="success" size="small" @click="openPass(scope.row.id)">通过</el-button>
            <el-button type="danger" size="small" @click="openReject(scope.row.id)">驳回</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 待审核分页 -->
    <el-pagination
      v-if="tab === 'wait'"
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      @current-change="getWaitList"
      @size-change="getWaitList"
      style="margin-top:20px"
    />

    <!-- 通过弹窗 -->
    <el-dialog v-model="passVisible" title="审核通过">
      <el-input v-model="passComment" placeholder="填写审核意见"></el-input>
      <template #footer>
        <el-button @click="passVisible=false">取消</el-button>
        <el-button type="primary" @click="submitPass">确定</el-button>
      </template>
    </el-dialog>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectVisible" title="驳回">
      <el-input v-model="rejectComment" placeholder="必须填写驳回意见"></el-input>
      <template #footer>
        <el-button @click="rejectVisible=false">取消</el-button>
        <el-button type="danger" @click="submitReject">确定驳回</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="资质详情">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="文件名称">{{ detail.fileName }}</el-descriptions-item>
        <el-descriptions-item label="院校名称">{{ detail.institutionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="院校ID">{{ detail.institutionId }}</el-descriptions-item>
        <el-descriptions-item label="材料类型">{{ materialTypeMap[detail.materialType] || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="文件大小">{{ formatFileSize(detail.fileSize) }}</el-descriptions-item>
        <el-descriptions-item label="文件地址">{{ detail.fileUrl }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag v-if="detail.reviewStatus === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="detail.reviewStatus === 1" type="success">已通过</el-tag>
          <el-tag v-else type="danger">已驳回</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见">{{ detail.reviewComment || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWaitAuditApi, getAuditHistoryApi, getQualDetailApi, auditPassApi, auditRejectApi } from '../assets/api/index'

const tab = ref('wait')
const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const searchInstId = ref('')
const currentId = ref('')
const passVisible = ref(false)
const rejectVisible = ref(false)
const detailVisible = ref(false)
const passComment = ref('')
const rejectComment = ref('')
const detail = ref({})

// 材料类型映射
const materialTypeMap = {
  1: '办学许可证',
  2: '营业执照',
  3: '组织机构代码证',
  4: '法人身份证',
  5: '其他资质'
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

onMounted(() => getWaitList())

// tab 切换
const onTabChange = () => {
  pageNum.value = 1
  if (tab.value === 'wait') getWaitList()
  else getHistory()
}

// 搜索
const searchList = () => {
  pageNum.value = 1
  getWaitList()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  searchInstId.value = ''
  pageNum.value = 1
  getWaitList()
}

// 待审核列表
const getWaitList = async () => {
  const keyword = searchKeyword.value || undefined
  const instId = searchInstId.value ? Number(searchInstId.value) : undefined
  const res = await getWaitAuditApi(pageNum.value, pageSize.value, keyword, instId)
  if (res && res.records) {
    list.value = res.records
    total.value = res.total
  } else {
    list.value = res || []
    if (list.value.length === pageSize.value) {
      total.value = pageNum.value * pageSize.value + 1
    } else {
      total.value = (pageNum.value - 1) * pageSize.value + list.value.length
    }
  }
}

// 审核历史
const getHistory = async () => {
  const keyword = searchKeyword.value || undefined
  const res = await getAuditHistoryApi(keyword)
  if (res && res.records) {
    list.value = res.records
    total.value = res.total
  } else {
    list.value = res || []
    if (list.value.length === pageSize.value) {
      total.value = pageNum.value * pageSize.value + 1
    } else {
      total.value = (pageNum.value - 1) * pageSize.value + list.value.length
    }
  }
}

// 查看详情
const openDetail = async (id) => {
  detail.value = await getQualDetailApi(id)
  detailVisible.value = true
}

// 打开通过弹窗
const openPass = (id) => {
  currentId.value = id
  passComment.value = ''
  passVisible.value = true
}

// 提交通过
const submitPass = async () => {
  await auditPassApi(currentId.value, passComment.value)
  ElMessage.success('审核完成')
  passVisible.value = false
  getWaitList()
}

// 打开驳回弹窗
const openReject = (id) => {
  currentId.value = id
  rejectComment.value = ''
  rejectVisible.value = true
}

// 提交驳回
const submitReject = async () => {
  if (!rejectComment.value) return ElMessage.warning('驳回必须填写意见')
  await auditRejectApi(currentId.value, rejectComment.value)
  ElMessage.success('已驳回')
  rejectVisible.value = false
  getWaitList()
}
</script>
