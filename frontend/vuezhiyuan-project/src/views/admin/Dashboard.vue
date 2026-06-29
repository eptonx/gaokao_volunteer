<template>
  <div class="dashboard">
    <div class="header">
      <h2>运营端工作台</h2>
      <el-button @click="loadAll" :loading="refreshing" size="small">刷新数据</el-button>
    </div>

    <!-- 8 统计卡片 -->
    <el-row :gutter="16">
      <el-col :span="6" v-for="c in cards" :key="c.label">
        <el-card class="stat-card" :style="{borderTop:'3px solid '+c.color}">
          <div class="stat-num" :style="{color:c.color}">{{ c.value.toLocaleString() }}</div>
          <div class="stat-label">{{ c.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第一行图表 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="8">
        <el-card><template #header>近30天活跃趋势</template><div ref="c1" class="chart"></div></el-card>
      </el-col>
      <el-col :span="8">
        <el-card><template #header>院校层次分布</template><div ref="c2" class="chart"></div></el-card>
      </el-col>
      <el-col :span="8">
        <el-card><template #header>简章审核概览</template><div ref="c3" class="chart"></div></el-card>
      </el-col>
    </el-row>

    <!-- 第二行图表 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <el-card><template #header>热门院校 Top10</template><div ref="c4" class="chart" style="height:380px"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><template #header>招生省份分布</template><div ref="c5" class="chart" style="height:380px"></div></el-card>
      </el-col>
    </el-row>

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getTrends, getTopInstitutions, getProvinceDistribution, getLevelDistribution, getReviewOverview } from '@/api/dashboard'

const PROVINCE_MAP = { '11':'北京','12':'天津','13':'河北','14':'山西','15':'内蒙古','21':'辽宁','22':'吉林','23':'黑龙江','31':'上海','32':'江苏','33':'浙江','34':'安徽','35':'福建','36':'江西','37':'山东','41':'河南','42':'湖北','43':'湖南','44':'广东','45':'广西','46':'海南','50':'重庆','51':'四川','52':'贵州','53':'云南','54':'西藏','61':'陕西','62':'甘肃','63':'青海','64':'宁夏','65':'新疆' }
const LEVEL_COLORS = { '985':'#e74c3c','211':'#f39c12','双一流':'#3498db','普通':'#95a5a6' }
const REVIEW_LABELS = { 0:'待审核', 1:'已通过', 2:'已驳回' }
const REVIEW_COLORS = { 0:'#e6a23c', 1:'#67c23a', 2:'#f56c6c' }
const CARD_COLORS = ['#409EFF','#67C23A','#E6A23C','#F56C6C','#409EFF','#67C23A','#E6A23C','#909399']

const cards = ref([
  { label:'用户总数', value:0, color:'#409EFF' },
  { label:'院校总数', value:0, color:'#67C23A' },
  { label:'已发布计划', value:0, color:'#E6A23C' },
  { label:'今日活跃', value:0, color:'#F56C6C' },
  { label:'待审简章', value:0, color:'#E6A23C' },
  { label:'已上线院校', value:0, color:'#67C23A' },
  { label:'志愿方案', value:0, color:'#409EFF' },
  { label:'今日新增用户', value:0, color:'#909399' },
])
const c1=ref(null), c2=ref(null), c3=ref(null), c4=ref(null), c5=ref(null)
const refreshing = ref(false)
let charts = []

function render(dom, opt) {
  if (!dom) return
  const c = echarts.init(dom)
  c.setOption(opt)
  charts.push(c)
}

function pn(code) {
  const s = String(code||'')
  return PROVINCE_MAP[s.substring(0,2)] || s
}

async function loadAll() {
  refreshing.value = true

  try {
    const o = await getOverview()
    cards.value[0].value = o.userCount || 0
    cards.value[1].value = o.institutionCount || 0
    cards.value[2].value = o.publishedPlanCount || 0
    cards.value[3].value = o.todayActive || 0
    cards.value[4].value = o.pendingGuideCount || 0
    cards.value[5].value = o.onlineInstitutionCount || 0
    cards.value[6].value = o.planCount || 0
    cards.value[7].value = o.todayNewUsers || 0
  } catch(e){}

  try {
    const t = await getTrends()
    const d = t.trends||[]
    render(c1.value, {
      tooltip:{trigger:'axis'}, grid:{top:20,left:40,right:10,bottom:20},
      xAxis:{type:'category',data:d.map(i=>i.date)},
      yAxis:{type:'value',minInterval:1},
      series:[{data:d.map(i=>i.count),type:'line',smooth:true,areaStyle:{color:'rgba(64,158,255,0.15)'},color:'#409EFF'}]
    })
  } catch(e){}

  try {
    const l = await getLevelDistribution()
    const dl = l.levels||[]
    render(c2.value, {
      tooltip:{trigger:'item',formatter:'{b}: {c}所 ({d}%)'},
      legend:{orient:'horizontal',bottom:5,textStyle:{fontSize:12}},
      series:[{type:'pie',radius:['45%','70%'],center:['50%','50%'],
        label:{show:true,position:'outside',formatter:'{b}\n{c}所',fontSize:11},
        data:dl.map(i=>({name:i.level,value:i.count,itemStyle:{color:LEVEL_COLORS[i.level]||'#bbb'}}))}]
    })
  } catch(e){}

  try {
    const r = await getReviewOverview()
    const dr = r.review||[]
    const total = dr.reduce((s,i)=>s+i.count,0)||1
    render(c3.value, {
      tooltip:{trigger:'item'},
      series:[{type:'pie',radius:['55%','75%'],center:['50%','55%'],label:{formatter:'{b}\n{c}份'},
        data:dr.map(i=>({name:REVIEW_LABELS[i.status]||'未知',value:i.count,itemStyle:{color:REVIEW_COLORS[i.status]||'#bbb'}}))}],
      graphic: total>0 ? [{type:'text',left:'center',top:'42%',style:{text:'共'+total+'份',textAlign:'center',fill:'#666',fontSize:16}}] : []
    })
  } catch(e){}

  try {
    const tp = await getTopInstitutions()
    const dt = (tp.top10||[]).slice().reverse()
    render(c4.value, {
      tooltip:{trigger:'axis'}, grid:{left:'28%',right:'5%',bottom:10,top:10},
      xAxis:{type:'value',minInterval:1},
      yAxis:{type:'category',data:dt.map(i=>i.name),axisLabel:{width:100,overflow:'truncate'}},
      series:[{data:dt.map(i=>({value:i.count,itemStyle:{color:LEVEL_COLORS[i.level]||'#409EFF'}})),type:'bar',label:{show:true,position:'right'}}]
    })
  } catch(e){}

  try {
    const p = await getProvinceDistribution()
    const raw = p.distribution||[]
    const merged = {}
    raw.forEach(i=>{ const n=pn(i.province); merged[n]=(merged[n]||0)+(i.count||0) })
    const names = Object.keys(merged), counts = names.map(n=>merged[n])
    render(c5.value, {
      tooltip:{trigger:'axis'}, grid:{left:40,right:20,bottom:60,top:10},
      xAxis:{type:'category',data:names,axisLabel:{rotate:45}},
      yAxis:{type:'value',min:0,minInterval:1},
      series:[{data:counts,type:'bar',color:'#67C23A',barMaxWidth:40,label:{show:true,position:'top'}}]
    })
  } catch(e){}

  refreshing.value = false
}

onMounted(()=>{ loadAll(); window.addEventListener('resize',()=>charts.forEach(c=>c.resize())) })
onUnmounted(()=>charts.forEach(c=>c.dispose()))
</script>

<style scoped>
.dashboard { padding: 0; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.header h2 { margin: 0; font-size: 20px; }
.stat-card { text-align: center; cursor: default; }
.stat-num { font-size: 32px; font-weight: bold; }
.stat-label { color: #909399; margin-top: 6px; font-size: 13px; }
.chart { width: 100%; height: 280px; }
</style>
