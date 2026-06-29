import request from '@/utils/request'

export const getOverview = () => request.get('/dashboard/overview')
export const getTrends = () => request.get('/dashboard/trends')
export const getTopInstitutions = () => request.get('/dashboard/top-institutions')
export const getProvinceDistribution = () => request.get('/dashboard/province-distribution')
export const getLevelDistribution = () => request.get('/dashboard/level-distribution')
export const getReviewOverview = () => request.get('/dashboard/review-overview')
export const getRecentLogins = () => request.get('/dashboard/recent-logins')
