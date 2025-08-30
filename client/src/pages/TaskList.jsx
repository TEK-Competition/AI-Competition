import React,  { useState, useEffect }  from 'react';
import { Table, Input, Button, Pagination,  message,  Spin } from 'antd';
import axios from 'axios';
import { BookOutlined} from '@ant-design/icons';
import { Link } from'react-router-dom';
import {getTaskTypeValue,  getTaskStatusValue, getTaskStepsValue } from './types/category';


const TaskList = () => {

    const [searchText, setSearchText] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [records, setRecords] = useState([]);
    const pageSize = 10;
    const [totalPage, setTotalPage] = useState(10);
    const [isLoading, setIsLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();
      
    const columns = [
    {
        title: 'Id',
        dataIndex: 'id',
        key: 'id',
    },
    {
        title: '项目ID',
        dataIndex: 'itemId',
        key: 'itemId',
    },
    {
        title: '名称',
        dataIndex: 'name',
        key: 'name',
    },
    {
        title: '运行模式',
        dataIndex: 'type',
        key: 'type',
        render: (type) => {
      
         return  getTaskTypeValue(type);
      },
    },
    {
        title: '状态',
        dataIndex: 'status',
        key: 'status',
        render: (status) => {

        return getTaskStatusValue(status);
      },
    },
    {
      title: '步骤',
      dataIndex: 'steps',
      key: 'steps',
      render: (steps) => {

        return getTaskStepsValue(steps);
    },
    },
    {
        title: '开始时间',
        dataIndex: 'startTime',
        key: 'startTime',
    },
    {
        title: '结束时间',
        dataIndex: 'endTime',
        key: 'endTime',
    },
    {
        title: '操作',
        key: 'operation',
        render: (idx, record) => (
        <span>
             <Link to={`/task-detail/${record.id}`}>
                <Button type="primary" size="small" icon={<BookOutlined />} style={{ marginRight: 8 }}>
                    查看
                </Button>
            </Link>
        </span>
        ),
    },
    ];

    useEffect(() => {
        async function fetchData() {
            await loadRecord();
        }
        fetchData();
    }, []); // Or [] if effect doesn't need props or state


    const loadRecord = async () => {
     
      try {
        const params ={name:searchText, pageNum:1, pageSize:10};
        setIsLoading(true);
        const res = await fetchRecords(params);
        console.log("load data result:", res);
        setRecords(res.data.data.list);
        setTotalPage(res.data.data.totalElements);
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


    // 处理查询
    const handleSearch = async () => {
     
        let param = {
            pageNum:1,
            pageSize:10
        }
        if (searchText === "") {
            param.name = '';
        } else {
            param.name = searchText;
        }
        console.log("params", param);
        await loadRecord();

    }; 

    // 处理分页变化
    const handlePageChange = (page) => {
        setCurrentPage(page);
    };

 

   //获取数据
   const fetchRecords = async (param) => {
       const result = await axios.post('http://8.134.218.222:7000/task/query', param)
       return result;
   }
    
    return ( 
        <div>
            {contextHolder}
            <Spin spinning={isLoading} fullscreen tip="加载中..." />
            <h2>任务列表</h2>
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 }}>
                <Input 
                placeholder="请输入名称" 
                value={searchText} 
                onChange={(e) => setSearchText(e.target.value)} 
                style={{ marginRight: 8, width: 200 }}
                />
                <Button type="primary" onClick={handleSearch}>
                  搜索
                </Button>
            </div>
            <Table 
                columns={columns} 
                dataSource={records}
                pagination={false} 
                bordered 
            />
            <Pagination 
                current={currentPage} 
                pageSize={pageSize} 
                total={totalPage} 
                onChange={handlePageChange} 
                style={{ marginTop: 16, textAlign: 'right' }}
            />
           
        </div>
    )
    
};

export default TaskList;