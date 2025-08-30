

import React,  { useState, useEffect }  from 'react';
import { Table, Button, message} from 'antd';
import axios from 'axios';
import {  BookOutlined} from '@ant-design/icons';
import { Link } from'react-router-dom';


const ProjectFileInfo = ({
    id
}) => {

  const [records, setRecords] = useState([]);
  const [messageApi, contextHolder] = message.useMessage();


  const columns = [
    {
        title: 'Id',
        dataIndex: 'id',
        key: 'id',
    },
    {
        title: '文件名称',
        dataIndex: 'name',
        key: 'name',
        
    },
    {
        title: '操作',
        key: 'operation',
        render: (idx, record) => (
        <span>
             <Link to={`/file-detail/${record.id}`}>
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
    }, []); 


  const loadRecord = async () => {
    try {
        const params ={id:id, name:'', type: ''};
        const res = await fetchRecords(params);
        console.log("load data result:", res);
        setRecords(res.data.data);
      } catch (error) {
        console.log("load data error:", error);
        messageApi.open({
            type: 'error',
            content: `${error}`,
        });
      }
  }


  //获取数据
  const fetchRecords = async (param) => {
    const result = await axios.post('http://8.134.218.222:7000/project/file/list', param)
    console.log("fetch project file data:", result);
    return result;
  }

   return (
     <div>
        {contextHolder}        
        <Table 
            columns={columns} 
            dataSource={records}
            pagination={false} 
            bordered 
        />
     </div>
   );

}

export default ProjectFileInfo;