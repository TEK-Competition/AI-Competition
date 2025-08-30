
import React,  { useState, useEffect }  from 'react';
import { Card, Tabs, Button, message, Typography, Space, Modal, Spin} from 'antd';
import { CopyOutlined, CheckOutlined } from '@ant-design/icons';
import { Prism as SyntaxHighlighter } from'react-syntax-highlighter';
import { vscDarkPlus } from'react-syntax-highlighter/dist/esm/styles/prism';
import 'antd/dist/reset.css';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import {  QuestionCircleOutlined } from '@ant-design/icons';


const { Title } = Typography;
const { TabPane } = Tabs;


const FileDetail = () => {

   const { id } = useParams();
   // 复制状态管理
   const [copyStatus, setCopyStatus] = useState({
    sp: false,
    sql: false
  });
  const [messageApi, contextHolder] = message.useMessage();
  const [detail, setDetail] = useState({id:'', name:'', sp:'', sql:''});
  const [showRenewFileView, setShowRenewFileView] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  

useEffect(() => {
    async function fetchData() {
        try {
            setIsLoading(true);
            const res = await getFileDetail(id);
            console.log("res:", res);
            if (res.data.success) {
                setDetail(res.data.data)
            } else {
                messageApi.open({
                    type: 'error',
                    content: `${res.data.msg}`,
                });
            }
            setIsLoading(false);
        } catch (error) {
            console.log("load file detail error:", error);
            setIsLoading(false);
            messageApi.open({
                type: 'error',
                content: `${error}`,
            });
        }
    }
    fetchData();
}, []); 


const showRenewFileModel = () => {
    setShowRenewFileView(true);
}

const handleRenewViewModelCancel = () => {
  setShowRenewFileView(false);
}

const handleRenewFile = async () => {
    try {
       const res = await renewFile(id, detail.id);
       if (res.data.success) {
           messageApi.open({
               type: 'success',
               content: '任务文件重置成功！',
           });
       } else {
           messageApi.open({
               type: 'error',
               content: `${res.data.msg}`,
           });
       }
    } catch (error) {
       messageApi.open({
           type: 'error',
           content: `${error}`,
       });
    }
    setShowRenewFileView(false);
 }


 // 复制代码到剪贴板
 const handleCopy = (type) => {
  const text = detail[type];
  navigator.clipboard.writeText(text)
   .then(() => {
      setCopyStatus(prev => ({...prev, [type]: true }));
      message.success(`已复制${type === 'sp'? '存储过程' : 'SparkSQL'}代码`);
      setTimeout(() => setCopyStatus(prev => ({...prev, [type]: false })), 2000);
    })
   .catch(() => message.error('复制失败，请手动复制'));
};

// 处理代码中的```标记（移除包裹标记，只保留纯SQL）
const processSqlText = (text) => {
  if (text.includes('```sql')) {
    return text.replace('```sql', '').replace('```', '').trim();
  }
  return text;
};

const getFileDetail = async (fileId) => {
    const result = await axios.get(`http://8.134.218.222:7000/project/file/detail/${fileId}`)
    return result;
} 

const renewFile = async (id, fileId) => {
    const result = await axios.post(`http://8.134.218.222:7000/task/renew?itemId=${id}&fileId=${fileId}`)
    console.log("renew project file:", result);
    return result;
}


  return (
    <div>
        {contextHolder} 
        <Spin spinning={isLoading} fullscreen tip="加载中..." />  
        <div style={{ padding: '24px', background: '#f5f5f5' }}>

            <Title level={3} style={{ marginBottom: '24px' }}>SQL代码展示</Title>
            <Button key="submit" type="primary" disabled={detail.id === '' ? true : false} onClick={showRenewFileModel}>
                重置任务文件
            </Button>
            <Tabs defaultActiveKey="sp" size="large">
            {/* 存储过程标签页 */}
            <TabPane tab={<span>存储过程：{detail.name}</span>} key="sp">
                <Card>
                <Space style={{ marginBottom: '16px', float: 'right' }}>
                    <Button
                    type="text"
                    icon={copyStatus.sp? <CheckOutlined style={{ color: '#52c41a' }} /> : <CopyOutlined />}
                    onClick={() => handleCopy('sp')}
                    >
                    {copyStatus.sp? '已复制' : '复制代码'}
                    </Button>
                </Space>
                
                <SyntaxHighlighter
                    language="sql"
                    style={vscDarkPlus} // 使用VS Code风格的暗色主题
                    showLineNumbers={true} // 显示行号
                    lineNumberStyle={{ color: '#888', fontSize: '12px' }}
                    wrapLongLines={true} // 自动换行
                    customStyle={{
                    borderRadius: '6px',
                    padding: '16px',
                    fontSize: '14px',
                    lineHeight: '1.6',
                    marginTop: '16px'
                    }}
                >
                    {detail.sp}
                </SyntaxHighlighter>
                </Card>
            </TabPane>

            {/* SparkSQL标签页 */}
            <TabPane tab="SparkSQL转换代码" key="sql">
                <Card>
                <Space style={{ marginBottom: '16px', float: 'right' }}>
                    <Button
                    type="text"
                    icon={copyStatus.sql? <CheckOutlined style={{ color: '#52c41a' }} /> : <CopyOutlined />}
                    onClick={() => handleCopy('sql')}
                    >
                    {copyStatus.sql? '已复制' : '复制代码'}
                    </Button>
                </Space>
                
                <SyntaxHighlighter
                    language="sql"
                    style={vscDarkPlus}
                    showLineNumbers={true}
                    lineNumberStyle={{ color: '#888', fontSize: '12px' }}
                    wrapLongLines={true}
                    customStyle={{
                    borderRadius: '6px',
                    padding: '16px',
                    fontSize: '14px',
                    lineHeight: '1.6',
                    marginTop: '16px'
                    }}
                >
                    {processSqlText(detail.sql)}
                </SyntaxHighlighter>
                </Card>
            </TabPane>
            </Tabs>
       </div>
       <Modal
            title="重置任务文件"
            visible={showRenewFileView}
            onCancel={handleRenewViewModelCancel}
            footer={[
                <Button key="back" onClick={handleRenewViewModelCancel}>
                    取消
                </Button>,
                <Button key="submit" type="primary" onClick={handleRenewFile}>
                    确定
                </Button>,
                ]}
            destroyOnClose={true}
            maskClosable={false}
            width={400}
        >
            <div>
            <p>
                <QuestionCircleOutlined style={{color:'#1476fe', fontSize:25}}/> 您确定要重置该文件吗？
            </p>
            <p>{detail.name}</p>
            </div>
        </Modal> 
  </div>
  );
};

export default FileDetail;