<template>
  <div class="login-record">
    <el-card>
      <el-form :inline="true">
        <el-form-item label="用户ID">
          <el-input v-model="search.userId" placeholder="用户ID" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="登录状态">
          <el-select v-model="search.status" placeholder="全部" clearable style="width:120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchPage(1)">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:12px">
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="用户类型" width="80">
          <template #default="{ row }">{{ {1:'考生',2:'院校',3:'管理员'}[row.userType] || row.userType }}</template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="deviceInfo" label="设备" min-width="140" show-overflow-tooltip />
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'成功':'失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="失败原因" min-width="120" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="160" />
      </el-table>

      <el-pagination
        style="margin-top:12px;justify-content:flex-end"
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchPage"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { getLoginRecordPage } from '@/api/loginRecord'

const list = ref([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const search = reactive({ userId: '', status: null })

async function fetchPage(p) {
  pageNum.value = p || 1
  loading.value = true
  try {
    const res = await getLoginRecordPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      userId: search.userId || undefined,
      status: search.status !== null && search.status !== '' ? search.status : undefined
    })
    list.value = res.list || []
    total.value = res.total || 0
  } finally { loading.value = false }
}

fetchPage(1)
</script>
