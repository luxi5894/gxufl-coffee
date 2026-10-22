import { ElMessageBox } from 'element-plus'

export const confirmAction = async (message, title, options = {}) => {
  try {
    await ElMessageBox.confirm(message, title, {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      ...options
    })
    return true
  } catch (error) {
    return false
  }
}
