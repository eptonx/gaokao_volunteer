<template>
  <div class="guide-review">
    <!-- 待审核 -->
    <el-card>
      <template #header>待审核</template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="institutionName" label="院校名称" width="150" />
        <el-table-column prop="title" label="简章标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="year" label="年份" width="70" />
        <el-table-column label="发布" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.publishStatus === 1" type="success" size="small">已发布</el-tag>
            <el-tag v-else type="info" size="small">草稿</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览" width="70" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openDetail(row.id)">审核</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 审核历史 -->
    <el-card style="margin-top:16px">
      <template #header>审核历史</template>
      <el-table :data="reviewed" v-loading="loading2" stripe size="small">
        <el-table-column prop="institutionName" label="院校名称" width="150" />
        <el-table-column prop="title" label="简章标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="year" label="年份" width="70" />
        <el-table-column label="结果" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.reviewStatus === 1" type="success" size="small">通过</el-tag>
            <el-tag v-else type="danger" size="small">驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reviewComment" label="审核意见" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="提交时间" width="160" />
      </el-table>
    </el-card>

    <!-- 审核弹窗 -->
    <el-dialog v-model="dialogVisible" title="招生简章审核" width="900px" top="30px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="院校">{{ detail.institutionName }}</el-descriptions-item>
        <el-descriptions-item label="年份">{{ detail.year }}</el-descriptions-item>
        <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="原始文件">{{ detail.sourceFileName }}</el-descriptions-item>
        <el-descriptions-item label="发布状态">
          <el-tag v-if="detail.publishStatus===1" type="success" size="small">已发布</el-tag>
          <el-tag v-else type="info" size="small">草稿</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ detail.viewCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="下载量">{{ detail.downloadCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="简章内容" :span="2">
          <div style="max-height:200px;overflow-y:auto;white-space:pre-wrap;font-size:13px;background:#fafafa;padding:8px;border-radius:4px">{{ detail.content }}</div>
        </el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <div style="display:flex;gap:8px;align-items:center">
          <el-input v-model="reviewComment" placeholder="驳回请填写理由（必填）" size="small" style="flex:1" clearable />
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="danger" @click="doReview(2)" :disabled="!reviewComment">驳回</el-button>
          <el-button type="success" @click="doReview(1)">通过</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getReviewList, getReviewedList, getDetail, review } from '@/api/admissionGuide'

const list = ref([])
const reviewed = ref([])
const loading = ref(false)
const loading2 = ref(false)
const dialogVisible = ref(false)
const detail = ref({})
const reviewComment = ref('')

async function fetchList() {
  loading.value = true
  try {
    const res = await getReviewList()
    list.value = Array.isArray(res) ? res : []
  } finally { loading.value = false }
}

async function fetchReviewed() {
  loading2.value = true
  try {
    const res = await getReviewedList()
    reviewed.value = Array.isArray(res) ? res : []
  } finally { loading2.value = false }
}

async function openDetail(id) {
  try {
    detail.value = await getDetail(id) || {}
    reviewComment.value = ''
    dialogVisible.value = true
  } catch (e) { ElMessage.error('获取详情失败') }
}

async function doReview(status) {
  try {
    await review(detail.value.id, status, reviewComment.value || null, 1)
    ElMessage.success(status === 1 ? '已通过' : '已驳回')
    dialogVisible.value = false
    fetchList()
    fetchReviewed()
  } catch (e) { ElMessage.error('审核失败') }
}

fetchList()
fetchReviewed()
</script>
