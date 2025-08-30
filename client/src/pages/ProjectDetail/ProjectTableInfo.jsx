

import React,  { useState, useEffect }  from 'react';
import { Table, Button,  Modal,  message } from 'antd';
import axios from 'axios';
import { BookOutlined} from '@ant-design/icons';


const ProjectTableInfo = ({
    id
}) => {

  const [records, setRecords] = useState([]);
  const [fieldRecords, setFieldRecords] = useState([]);
  const [isFieldRecordVisible, setIsFieldRecordVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const columns = [
    {
        title: 'Id',
        dataIndex: 'id',
        key: 'id'
    },
    {
        title: '表名',
        dataIndex: 'name',
        key: 'name',
        
    },
    {
        title: '操作',
        key: 'operation',
        render: (idx, record) => (
        <span>
            <Button type="primary" size="small" icon={<BookOutlined />} style={{ marginRight: 8 }} onClick={() => {
                showViewModal(record)
            }}>
            查看
            </Button>
        </span>
        ),
    },
    ];


    const fieldColumns = [
        // {
        //     title: 'Id',
        //     dataIndex: 'id',
        //     key: 'id',
        // },
        {
            title: '字段名',
            dataIndex: 'name',
            key: 'name',
            
        },
        {
            title: '数据类型',
            dataIndex: 'dataType',
            key: 'dataType',
            
        },
        {
            title: '字段说明',
            dataIndex: 'description',
            key: 'description'   
        }
        ];


    useEffect(() => {
        async function fetchData() {
            await loadRecord();
        }
        fetchData();
    }, []); 


 // 显示弹窗
 const showViewModal = async (updatingRecord) => {
    await loadFieldRecord(updatingRecord.id);
    setIsFieldRecordVisible(true);
};

  const handleViewModelCancel = () => {
     setFieldRecords([]);
     setIsFieldRecordVisible(false);
     
  }

  const loadRecord = async () => {
    
    try {
        const params ={id: id, name:'', type: ''};
        setIsLoading(true);
        const res = await fetchRecords(params);
        setRecords(res.data.data);
        setIsLoading(false);
      } catch (error) {
        console.log("load data error:", error);
        setIsLoading(false);
        message.error(error);
      }
  }

  const loadFieldRecord = async (tableId) => {
    try {
        const params ={id: tableId, name:''};
        setIsLoading(true);
        const res = await fetchFieldRecords(params);
        setFieldRecords(res.data.data);
        setIsLoading(false);
      } catch (error) {
        console.log("load data error:", error);
        setIsLoading(false);
        message.error(error);
      }
  }

  //获取数据
  const fetchRecords = async (param) => {
    const result = await axios.post('http://8.134.218.222:7000/project/table/list', param)
    console.log("fetch table data:", result);
    return result;
  }

   //获取表字段数据
   const fetchFieldRecords = async (param) => {
    const result = await axios.post('http://8.134.218.222:7000/project/field/list', param)
    console.log("fetch table field data:", result);
    return result;
  }

   return (
     <div>
        <Table 
            columns={columns} 
            dataSource={records}
            pagination={false} 
            bordered 
        />
         <Modal
                title="字段信息"
                visible={isFieldRecordVisible}
                onCancel={handleViewModelCancel}
                footer={[
                <Button key="back" onClick={handleViewModelCancel}>
                    取消
                </Button>
                ]}
                destroyOnClose={true}
                maskClosable={false}
                width={800}
            >
            <Table 
                columns={fieldColumns} 
                dataSource={fieldRecords}
                pagination={false} 
                bordered 
            />
            </Modal> 
     </div>
   );

}

export default ProjectTableInfo;