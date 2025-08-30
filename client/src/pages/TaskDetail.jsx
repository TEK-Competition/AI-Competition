import React,  { useState, useEffect }  from 'react';
import { Button, message, Spin , Descriptions, Steps } from 'antd';
import { useParams } from 'react-router-dom';
import {getTaskTypeValue,  getTaskStatusValue, getTaskStepsValue, getStepStatus } from './types/category'
import axios from 'axios';

const TaskDetail = () => {
  const { id } = useParams();
  const [isLoading, setIsLoading] = useState(false);
  const [detail, setDetail] = useState({id: '', itemId: '', name: '', type: '', status: '', steps: '', startTime: '', stages:[]});
  const [stages, setStages] = useState([]);
  const [messageApi, contextHolder] = message.useMessage();

  
  useEffect(() => {
    async function fetchData() {
        await loadRecord();
    }
    fetchData();
  }, []);
  

  const loadRecord = async () => {
    try {
      setIsLoading(true);
      const res = await fetchRecords();
      console.log("load data result:", res);
      setDetail(res.data.data);
      const stageRecord = res.data.data.stages;
      const stageList = [];
      for (let index = 0; index < stageRecord.length; index++) {
        const element = stageRecord[index];
        let fullTime = `开始时间：${element.startTime },  完成时间：${element.finishTime}`;
        let startTime = `开始时间：${element.startTime }`;
        let finishTime = `完成时间：${element.finishTime}`;
        let description = '';
        if (element.startTime !== undefined && element.finishTime !== undefined) {
          description = fullTime;
        } else if (element.finishTime !== undefined) {
          description = finishTime;
        } else if (startTime !== undefined && element.finishTime === undefined) {
          description = startTime;
        }  else {
          description = '';
        }
        let obj = {
          title: getTaskStepsValue(element.stage),
          status: getStepStatus(element.status),
          description: description
        }
        stageList.push(obj);
      }
      setStages(stageList);
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

  const refresh = async () => {
     await loadRecord();
  }


  //获取数据
  const fetchRecords = async () => {
    const result = await axios.get(`http://8.134.218.222:7000/task/detail/${id}`)
    return result;
  }
  
  return (
    <div>
       {contextHolder}
       <Spin spinning={isLoading} fullscreen tip="加载中..." />
        <h2>任务详情</h2>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 , marginTop: 10}}>
          <Descriptions title="基本信息" bordered={true}>
            <Descriptions.Item label="ID">{detail.id}</Descriptions.Item>
            <Descriptions.Item label="项目ID">{detail.itemId}</Descriptions.Item>
            <Descriptions.Item label="名称">{detail.name}</Descriptions.Item>
            <Descriptions.Item label="运行模式">{getTaskTypeValue(detail.type)}</Descriptions.Item>
            <Descriptions.Item label="状态">{getTaskStatusValue(detail.status)}</Descriptions.Item>
            <Descriptions.Item label="步骤">{getTaskStepsValue(detail.steps)}</Descriptions.Item>
            <Descriptions.Item label="开始时间">{detail.startTime}</Descriptions.Item>
          </Descriptions>
        </div>
        <h3 style={{fontWeight: 'bold'}}>阶段状态</h3>
        <Steps
              className="site-navigation-steps"
              direction="vertical"
              current={detail.steps}
              items={stages}
            />
            <div style={{paddingLeft: 150}}>
               <Button type="primary" onClick={refresh}>  刷新 </Button>
            </div>
          
                 
                
    </div>
  );
};

export default TaskDetail;