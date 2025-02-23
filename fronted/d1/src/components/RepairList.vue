<template>
  <div class="repair-list">
    <nav-bar />
    <main class="main-content">
      <div class="container">
        <div class="content-wrapper">
          <div class="header-section">
            <h2>报修清单</h2>
            <el-button type="primary" @click="showAddDialog">添加维修记录</el-button>
          </div>

          <!-- 维修列表表格 -->
          <el-table :data="repairList" style="width: 100%" border>
            <el-table-column prop="id" label="编号" width="80" />
            <el-table-column label="来源" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.type === 'maintenance' ? 'warning' : 'info'">
                  {{ scope.row.type === 'maintenance' ? '维修报修' : '清洁异常' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="位置" width="150" />
            <el-table-column prop="description" label="问题描述" />
            <el-table-column prop="reportTime" label="报修时间" width="180" />
            <el-table-column prop="reporter" label="报修人" width="120" />
            <el-table-column prop="urgency" label="紧急程度" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.urgency === '紧急' ? 'danger' : 'warning'">
                  {{ scope.row.urgency }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button 
                  size="small" 
                  @click="startRepair(scope.row)"
                  :disabled="scope.row.status !== '待处理'"
                >开始处理</el-button>
                <el-button 
                  size="small" 
                  type="primary"
                  @click="showEditDialog(scope.row)"
                  :disabled="scope.row.status === '已完成'"
                >填写过程</el-button>
                <el-button 
                  size="small" 
                  type="success" 
                  @click="completeRepair(scope.row)"
                  :disabled="scope.row.status !== '处理中'"
                >完成</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 添加/编辑维修记录对话框 -->
          <el-dialog 
            :title="dialogType === 'add' ? '添加维修记录' : '处理维修'" 
            v-model="dialogVisible"
            width="50%"
          >
            <el-form :model="repairForm" label-width="100px">
              <template v-if="dialogType === 'add'">
                <el-form-item label="维修位置" required>
                  <el-input v-model="repairForm.location" placeholder="请输入具体维修位置" />
                </el-form-item>
                <el-form-item label="问题描述" required>
                  <el-input 
                    type="textarea" 
                    v-model="repairForm.description" 
                    rows="3"
                    placeholder="请详细描述问题..."
                  />
                </el-form-item>
                <el-form-item label="报修人">
                  <el-input v-model="repairForm.reporter" placeholder="请输入报修人姓名" />
                </el-form-item>
              </template>
              <template v-else>
                <el-form-item label="维修位置">
                  <el-input v-model="repairForm.location" disabled />
                </el-form-item>
                <el-form-item label="问题描述">
                  <el-input type="textarea" v-model="repairForm.description" rows="3" disabled />
                </el-form-item>
                <el-form-item label="报修人">
                  <el-input v-model="repairForm.reporter" disabled />
                </el-form-item>
                <el-form-item label="维修过程" required>
                  <el-input 
                    type="textarea" 
                    v-model="repairForm.process" 
                    rows="3"
                    placeholder="请详细记录维修过程..."
                  />
                </el-form-item>
              </template>
            </el-form>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit">确定</el-button>
              </span>
            </template>
          </el-dialog>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import NavBar from './NavBar.vue'
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElButton, ElTable, ElTableColumn, ElTag, ElDialog, ElForm, ElFormItem, ElInput } from 'element-plus'

export default {
  name: 'RepairList',
  components: {
    NavBar,
    ElButton,
    ElTable,
    ElTableColumn,
    ElTag,
    ElDialog,
    ElForm,
    ElFormItem,
    ElInput
  },
  setup() {
    const repairList = ref([])
    const dialogVisible = ref(false)
    const dialogType = ref('add')
    const repairForm = reactive({
      id: '',
      location: '',
      description: '',
      reporter: '',
      process: ''
    })

    // 获取所有待处理的维修请求列表（包括清洁和维修）
    const fetchPendingRepairs = async () => {
      try {
        // 获取所有维修报修（不仅是待处理的）
        const maintenanceResponse = await fetch('/api/maintenance/my-records', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const maintenanceResult = await maintenanceResponse.json();

        // 获取清洁异常报修
        const cleaningResponse = await fetch('/api/cleaning-logs/abnormal', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const cleaningResult = await cleaningResponse.json();

        let allRepairs = [];

        // 处理维修报修数据
        if (maintenanceResult.success) {
          const maintenanceRepairs = maintenanceResult.data.map(item => ({
            id: item.id,
            type: 'maintenance', // 标记类型为维修
            location: `${item.buildingName || ''} - ${item.location}`,
            description: item.description,
            reportTime: new Date(item.createdAt).toLocaleString(),
            reporter: item.reporterName || '未知',
            status: item.status,
            process: item.result || '',
            urgency: item.urgency,
            images: item.imageUrls ? JSON.parse(item.imageUrls) : [],
            buildingId: item.buildingId
          }));
          allRepairs = allRepairs.concat(maintenanceRepairs);
        }

        // 处理清洁异常报修数据
        if (cleaningResult.success) {
          const cleaningRepairs = cleaningResult.data
            .filter(item => item.hasDamage || item.hygieneCondition === 'poor')
            .map(item => ({
              id: item.id,
              type: 'cleaning', // 标记类型为清洁
              location: item.area,
              description: item.hasDamage ? item.damageDesc : item.conditionDesc,
              reportTime: new Date(item.startTime).toLocaleString(),
              reporter: item.cleanerName,
              status: item.status || '待处理',
              process: item.repairResult || '',
              urgency: item.hasDamage ? '紧急' : '一般',
              images: item.imageUrls ? JSON.parse(item.imageUrls) : []
            }));
          allRepairs = allRepairs.concat(cleaningRepairs);
        }

        // 按报修时间排序
        allRepairs.sort((a, b) => new Date(b.reportTime) - new Date(a.reportTime));
        repairList.value = allRepairs;

      } catch (error) {
        console.error('获取报修请求失败:', error);
        ElMessage.error('获取报修请求列表失败');
      }
    }

    // 开始处理报修请求
    const startRepair = async (repair) => {
      try {
        const endpoint = repair.type === 'maintenance' 
          ? `/api/maintenance/${repair.id}/start`
          : `/api/cleaning-logs/${repair.id}/start-repair`;

        const response = await fetch(endpoint, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const result = await response.json();
        
        if (result.success) {
          ElMessage.success('已开始处理报修请求');
          repair.status = '处理中';
          await fetchPendingRepairs(); // 刷新列表
        } else {
          throw new Error(result.message);
        }
      } catch (error) {
        console.error('开始处理报修失败:', error);
        ElMessage.error('开始处理报修失败');
      }
    }

    // 完成报修
    const completeRepair = async (repair) => {
      try {
        if (!repair.process) {
          ElMessage.warning('请先填写处理过程');
          return;
        }

        const endpoint = repair.type === 'maintenance'
          ? `/api/maintenance/${repair.id}/complete`
          : `/api/cleaning-logs/${repair.id}/complete-repair`;

        const response = await fetch(endpoint, {
          method: 'PUT',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            result: repair.process,
            status: '已完成',
            handled_at: new Date()
          })
        });

        const result = await response.json();
        if (result.success) {
          ElMessage.success('报修已处理完成');
          repair.status = '已完成';
          await fetchPendingRepairs(); // 刷新列表
        } else {
          throw new Error(result.message);
        }
      } catch (error) {
        console.error('完成报修失败:', error);
        ElMessage.error('完成报修失败');
      }
    }

    const showEditDialog = (row) => {
      dialogType.value = 'edit'
      Object.keys(repairForm).forEach(key => repairForm[key] = row[key])
      dialogVisible.value = true
    }

    const showAddDialog = () => {
      dialogType.value = 'add'
      Object.keys(repairForm).forEach(key => repairForm[key] = '')
      dialogVisible.value = true
    }

    const handleSubmit = async () => {
      try {
        if (dialogType.value === 'add') {
          // 构造维修请求数据
          const formData = new FormData();
          formData.append('type', '其他');  // 默认类型
          formData.append('urgency', '一般');  // 默认紧急程度
          formData.append('buildingId', '1');  // 默认楼栋ID，你可能需要添加楼栋选择
          formData.append('location', repairForm.location);
          formData.append('description', repairForm.description);
          
          // 调用后端接口创建维修请求
          const response = await fetch('/api/maintenance', {
            method: 'POST',
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: formData
          });

          const result = await response.json();
          if (result.success) {
            ElMessage.success('添加维修记录成功');
            await fetchPendingRepairs(); // 刷新列表
          } else {
            throw new Error(result.message);
          }
        } else if (dialogType.value === 'edit') {
          // 更新维修过程
          const repair = repairList.value.find(item => item.id === repairForm.id);
          if (repair) {
            repair.process = repairForm.process;
            ElMessage.success('更新成功');
          }
        }
        dialogVisible.value = false;
      } catch (error) {
        console.error('操作失败:', error);
        ElMessage.error('操作失败: ' + error.message);
      }
    }

    const getStatusType = (status) => {
      const types = {
        '待处理': 'warning',
        '处理中': 'primary',
        '已完成': 'success'
      }
      return types[status] || 'info'
    }

    const getStatusText = (status) => {
      return status || '未知'
    }

    // 初始化时获取数据
    onMounted(() => {
      fetchPendingRepairs()
    })

    return {
      repairList,
      dialogVisible,
      dialogType,
      repairForm,
      showEditDialog,
      handleSubmit,
      completeRepair,
      getStatusType,
      getStatusText,
      startRepair,
      showAddDialog
    }
  }
}
</script>

<style scoped>
.repair-list {
  min-height: 100vh;
  background-color: #f7fafc;
}

.main-content {
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.content-wrapper {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

h2 {
  color: #2d3748;
  margin: 0;
}

.el-table {
  margin-top: 1rem;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}
</style> 