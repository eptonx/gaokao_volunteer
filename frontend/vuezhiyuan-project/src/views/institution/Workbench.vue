<template>
  <div class="pg">
    <h1>工作台</h1>
    <p class="sub">欢迎回来，以下是招生数据概览</p>
    <div class="cards">
      <div class="c"><b>📋</b><div><em>{{ ps.total }}</em><span>招生计划总数</span></div><small>已发布 {{ ps.published }} · 草稿 {{ ps.draft }}</small></div>
      <div class="c"><b>📄</b><div><em>{{ gs.total }}</em><span>招生简章总数</span></div><small>已发布 {{ gs.published }} · 草稿 {{ gs.draft }}</small></div>
      <div class="c"><b>📊</b><div><em>{{ ss.total }}</em><span>录取分数记录</span></div><small>历年录取分数线数据</small></div>
    </div>
    <div class="tbl">
      <h2>资质审核详情</h2>
      <table v-if="qs.list&&qs.list.length">
        <thead><tr><th>材料类型</th><th>文件名</th><th>审核状态</th><th>审核意见</th><th>提交时间</th></tr></thead>
        <tbody><tr v-for="q in qs.list" :key="q.id"><td><span class="tag">{{ MATERIAL_TYPES[q.materialType] || '其他' }}</span></td><td>{{ q.fileName }}</td><td><span :class="q.reviewStatus===1?'ok':q.reviewStatus===2?'fail':'wait'">{{ REVIEW_STATUSES[q.reviewStatus] || '待审核' }}</span></td><td>{{ q.reviewComment||'-' }}</td><td>{{ q.createdAt||'-' }}</td></tr></tbody>
      </table>
      <p v-else class="empty">暂无资质材料记录</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { overview } from '@/api/workbench'

const MATERIAL_TYPES = ['', '办学许可证', '营业执照', '组织机构代码证', '其他资质']
const REVIEW_STATUSES = ['待审核', '已通过', '已驳回']

const ps = reactive({ total:0, published:0, draft:0 })
const gs = reactive({ total:0, published:0, draft:0 })
const ss = reactive({ total:0 })
const qs = reactive({ total:0, approved:0, pending:0, rejected:0, list:[] })
// 从后端一次性拉概览数据，分别填进四个卡片
onMounted(async () => {
  const iid = localStorage.getItem('instInstitutionId')
  if (!iid || iid === 'null' || iid === 'undefined') return
  try {
    const d = await overview(Number(iid))
    Object.assign(ps, d.planStats || {})
    Object.assign(gs, d.guideStats || {})
    Object.assign(ss, d.scoreStats || {})
    Object.assign(qs, d.qualificationStats || {})
  } catch (e) {
    console.error('工作台查询失败:', e)
  }
})
</script>

<style scoped>
.pg h1 { font-size: 24px; font-weight: 700; color: #1a1a2e; }
.sub { color: #888; font-size: 14px; margin: 4px 0 24px; }
.cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; margin-bottom: 28px; }
.c {
  background: #fff; border-radius: 12px; padding: 24px; border: 1px solid #f0f0f0;
  display: flex; align-items: center; gap: 18px; flex-wrap: wrap;
}
.c b { font-size: 40px; }
.c div { display: flex; flex-direction: column; }
.c em { font-size: 34px; font-weight: 700; color: #1a1a2e; font-style: normal; }
.c span { font-size: 13px; color: #888; }
.c small { width: 100%; font-size: 12px; color: #aaa; padding-top: 8px; border-top: 1px solid #f5f5f5; margin-top: 4px; }
.tbl { background: #fff; border-radius: 12px; padding: 24px; border: 1px solid #f0f0f0; }
.tbl h2 { font-size: 17px; font-weight: 700; color: #1a1a2e; margin-bottom: 16px; }
table { width: 100%; border-collapse: collapse; }
th, td { padding: 11px 14px; text-align: left; border-bottom: 1px solid #f5f5f5; font-size: 14px; }
th { background: #fafafa; color: #888; font-weight: 600; font-size: 13px; }
td { color: #444; }
.tag { padding: 2px 10px; background: #f0f5ff; color: #2563eb; border-radius: 4px; font-size: 12px; }
.ok, .fail, .wait { padding: 2px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.ok { background: #f0fdf4; color: #059669; }
.fail { background: #fef2f2; color: #ef4444; }
.wait { background: #fffbeb; color: #d97706; }
.empty { text-align: center; color: #bbb; padding: 40px; font-size: 14px; }
</style>
