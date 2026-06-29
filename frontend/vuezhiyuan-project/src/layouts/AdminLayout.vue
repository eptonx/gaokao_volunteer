<script setup>
import { RouterView, useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <el-container style="min-height:100vh">
    <el-aside width="200px" style="background:#304156">
      <div style="color:#fff; text-align:center; padding:16px 0; font-size:18px; font-weight:bold">
        🎓 运营端
      </div>
      <el-menu
        :default-active="route.path"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <span>📊 仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <span>👤 管理员用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/qualificationAudit">
          <span>📋 资质审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/guide-review">
          <span>📝 招生简章审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/institution">
          <span>🏫 院校库管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/majorDict">
          <span>📚 专业库管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/admissionHistory">
          <span>📊 录取数据管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/login-record">
          <span>📜 登录日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-main>
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

