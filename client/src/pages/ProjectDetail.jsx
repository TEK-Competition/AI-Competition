import React,  { useState, useEffect }  from 'react';
import { Button, Modal, Select, message,  Spin, Descriptions, Tabs } from 'antd';
import axios from 'axios';
import { useParams } from 'react-router-dom';
import { FormOutlined,  LoadingOutlined, RotateRightOutlined, QuestionCircleOutlined} from '@ant-design/icons';
import GeneralFlowChart from '../component/GeneralFlowChart';
import ProjectFileInfo from './ProjectDetail/ProjectFileInfo';
import ProjectTableInfo from './ProjectDetail/ProjectTableInfo';
import { getProjectTypeValue, getProjectStatusValue,  stages} from './types/category'

const ProjectDetail = () => {

  // 使用 useParams 获取参数，返回值是一个对象
  const { id } = useParams();
  const [isLoading, setIsLoading] = useState(false);
  const [detail, setDetail] = useState({id: '', name: '', type:'', status:'', tableGraph:'', spGraph:'', remark:''});
  const { TabPane } = Tabs;
  const [activeKey, setActiveKey] = useState('file_info');
  const [showRenewTaskView, setShowRenewTaskView] = useState(false);
  const [showRenewTaskStageView, setShowRenewTaskStageView] = useState(false);
  const [stage, setStage] = useState('1');
  const [messageApi, contextHolder] = message.useMessage();


  useEffect(() => {
    async function fetchData() {
      await loadDetail();
    }
    fetchData();
}, []); // Or [] if effect doesn't need props or state


const loadDetail = async () => {     
  try {
    setIsLoading(true);
    const res = await fetchDetail();
    console.log("load project detail result:", res);
    setDetail(res.data.data)
    setIsLoading(false);
  } catch (error) {
    console.log("load data error:", error);
    setIsLoading(false);
    messageApi.open({
      type: 'error',
      content: `${error}`,
    });
  }

}

const handleRenewTask = async() => {
  try {
    const res = await renewTask();
    console.log("renew task :", res);
    if (res.data.success) {
      message.success("任务重置成功！");
      messageApi.open({
        type: 'success',
        content: '任务重置成功！',
      });
    } else {
      messageApi.open({
        type: 'error',
        content: '任务重置失败！',
      });
    }
  } catch (error) {
     messageApi.open({
      type: 'error',
      content: `${error}`,
    });
  }

  setShowRenewTaskView(false);

}

const handleRenewTaskViewModelCancel = () => {
    setShowRenewTaskView(false);
}


const handleRenewTaskStage = async () => {
    try {
       const res = await renewStage(id, stage);
       console.log(res);
       if (res.data.success) {
          messageApi.open({
            type: 'success',
            content: '任务阶段重置成功！',
          });
       }else {
          messageApi.open({
            type: 'error',
            content: '任务阶段重置失败！',
          });
       }
    } catch (error) {
      messageApi.open({
        type: 'error',
        content: `${error}`,
      });
    }
    setShowRenewTaskStageView(false);
} 

const handleRenewTaskStageViewModelCancel = () => {
   setShowRenewTaskStageView(false);
}


const handleStageChange = (value) => {
    setStage(value);
    console.log("value:", value);
    console.log("current stage:", stage);
}

// 处理标签切换
const handleTabChange = (key) => {
  setActiveKey(key);
};

//获取项目详情数据
const fetchDetail = async () => {
  const result = await axios.get(`http://8.134.218.222:7000/project/item/detail/${id}`)
  return result;
}
//重置任务
const renewTask = async () => {
  const result = await axios.post(`http://8.134.218.222:7000/task/renew?itemId=${id}`)
  return result;
}

//重置任务阶段
const renewStage = async (id, stage) => {
  const result = await axios.post(`http://8.134.218.222:7000/task/renew/stage?itemId=${id}&stage=${stage}`)
  return result;
}

// 第一组流程图的自定义配置
const tableFlowConfig = {
  title: "表关系图",
  height: 500,
  nodeStyles: {
    "NONE": {
      background: "#f6ffed",
      borderColor: "#52c41a"
    }
  },
  edgeOptions: {
    stroke: "#52c41a",
    strokeWidth: 1.5
  }
};

// 第二组流程图的自定义配置
const fileFlowConfig = {
  title: "文件关系图",
  height: 500,
  edgeOptions: {
    stroke: "#f5222d",
    strokeWidth: 1.5,
  },
  showMiniMap: false // 第二张图不显示迷你地图
};
 
// 渲染不同标签页内容
const renderTabContent = () => {
  if (isLoading) {
    return (
      <div style={{ textAlign: 'center', padding: '50px 0' }}>
        <Spin indicator={<LoadingOutlined spin size="large" />} tip="加载中..." />
      </div>
    );
  }

  switch (activeKey) {
    case 'table_relationship':
      return (
        <div>
           <GeneralFlowChart
              key='table'
              data={detail.tableGraph}
              {...tableFlowConfig}
            />
        </div>
      );
    
    case 'file_relationship':
      return (
        <div>
           <GeneralFlowChart
              key='file'
              data={detail.spGraph}
              {...fileFlowConfig}
            />
        </div>
      );
    
    case 'table_info':
      return (
        <ProjectTableInfo 
            id = {id}
        />
      );
    
    case'file_info':
      return (
        <ProjectFileInfo
            id = {id}
          />
      );
    
    default:
      return null;
  }
};

  return ( 
    <div>
        {contextHolder}
        <Spin spinning={isLoading} fullscreen tip="加载中..." />
        <h2>项目详情</h2>
        <div style={{marginBottom: 16, marginTop: 10}}>
           <Button type="primary" size="middle"  icon={<RotateRightOutlined />} style={{ marginRight: 8 }}
              onClick={() => { setShowRenewTaskView(true)  }} >
                重置任务
            </Button>
            <Button type="primary" size="middle" icon={<RotateRightOutlined />} style={{ marginRight: 8 }}
            onClick={() => { setShowRenewTaskStageView(true)  }}
            >
                重置任务阶段
            </Button>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 , marginTop: 10}}>
          <Descriptions title="基本信息" bordered={true}>
            <Descriptions.Item label="项目名称">{detail.name}</Descriptions.Item>
            <Descriptions.Item label="执行模式">{getProjectTypeValue(detail.type)}</Descriptions.Item>
            <Descriptions.Item label="执行状态">{getProjectStatusValue(detail.status)}</Descriptions.Item>
            <Descriptions.Item label="说明">{detail.remark}</Descriptions.Item>
          </Descriptions>
        </div>
      
        <div>
          <Tabs
            activeKey={activeKey}
            onChange={handleTabChange}
            type="card"  // 卡片式标签
            size="large"
            style={{ maxWidth: 800 }}
          >
            <TabPane tab={<><FormOutlined /> 表关系</>} key="table_relationship" />
            <TabPane tab={<><FormOutlined /> 文件关系</>} key="file_relationship" />
            <TabPane tab={<><FormOutlined /> 表信息</>} key="table_info" />
            <TabPane tab={<><FormOutlined />文件信息 </>} key="file_info" />
          </Tabs>
          
          <div style={{ marginTop: 24 }}>
            {renderTabContent()}
          </div>

        </div>
        <Modal
          title="重置任务"
          visible={showRenewTaskView}
          onCancel={handleRenewTaskViewModelCancel}
          footer={[
              <Button key="back" onClick={handleRenewTaskViewModelCancel}>
                  取消
              </Button>,
              <Button key="submit" type="primary" onClick={handleRenewTask}>
                  确定
              </Button>,
              ]}
          destroyOnClose={true}
          maskClosable={false}
          width={400}
      >
        <div>
          <p>
            <QuestionCircleOutlined style={{color:'#1476fe', fontSize:25}}/> 您确定要重置该项目的任务吗？
          </p>
        </div>
      </Modal> 

      <Modal
          title="重置任务阶段"
          visible={showRenewTaskStageView}
          onCancel={handleRenewTaskStageViewModelCancel}
          footer={[
              <Button key="back" onClick={handleRenewTaskStageViewModelCancel}>
                  取消
              </Button>,
              <Button key="submit" type="primary" onClick={handleRenewTaskStage}>
                  确定
              </Button>,
              ]}
          destroyOnClose={true}
          maskClosable={false}
          width={400}
      >
        <div style={{marginTop: 20, marginBottom:20}}>
        <span>任务阶段：</span>
        <Select
            defaultValue={stage}
            style={{ width: 120 }}
            onChange={handleStageChange}
            options={stages}
          />
        </div>
      </Modal> 
    </div>
)
};

export default ProjectDetail;