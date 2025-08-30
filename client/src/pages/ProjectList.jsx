import React,  { useState, useEffect }  from 'react';
import { Table, Input, Button, Pagination, Modal, Form, message, Radio, Upload, Popconfirm, Spin, notification } from 'antd';
import axios from 'axios';
import { UploadOutlined, FormOutlined, DeleteOutlined, BookOutlined} from '@ant-design/icons';
import { Link } from'react-router-dom';
import { getProjectTypeValue, getProjectStatusValue } from './types/category'

const ProjectList = () => {

    const [searchText, setSearchText] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [records, setRecords] = useState([]);
    const pageSize = 10;
    const [isAddRecordVisible, setIsAddRecordVisible] = useState(false);
    const [isEditRecordVisible, setIsEditRecordVisible] = useState(false);
    const [addForm] = Form.useForm();
    const [editForm] = Form.useForm();
    const [totalPage, setTotalPage] = useState(10);
    const [currentRecord, setCurrentRecord] = useState(null);
    const [deletingRoWIndex, setDeletingRowIndex] = useState();
    const [isLoading, setIsLoading] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();
      
    const columns = [
    {
        title: 'Id',
        dataIndex: 'id',
        key: 'id',
    },
    {
        title: '项目名称',
        dataIndex: 'name',
        key: 'name',
        
    },
    {
        title: '执行模式',
        dataIndex: 'type',
        key: 'type',
        render: (type) => {
            return getProjectTypeValue(type);
        },
    },
    {
        title: '执行状态',
        dataIndex: 'status',
        key: 'status',
        render: (status) => {
           return getProjectStatusValue(status);
        },
    },
    {
        title: '说明',
        dataIndex: 'remark',
        key: 'remark',
    },
    {
        title: '操作',
        key: 'operation',
        render: (idx, record) => (
        <span>
             <Link to={`/project-detail/${record.id}`}>
                <Button type="primary" size="small" icon={<BookOutlined />} style={{ marginRight: 8 }}>
                    查看
                </Button>
            </Link>
            <Button type="primary" size="small" icon={<FormOutlined />} style={{ marginRight: 8 }} onClick={() => {
                showEditModal(record)
            }}>
            编辑
            </Button>
            <Popconfirm
                placement="top"
                title='您确定想要删除这个项目吗？'
                description= {record.name}
                onConfirm={async(e) => {
                    await confirm(record, idx, e);
                }}
                okText="Yes"
                cancelText="No"
            >
                <Button danger loading={idx === deletingRoWIndex} icon={<DeleteOutlined />} size="small">
                    删除
                </Button>
            </Popconfirm>
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
        const params ={name:searchText, pageNum:1,pageSize:10};
        setIsLoading(true);
        const res = await fetchRecords(params);
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
        await loadRecord(param);
    }; 

    // 处理分页变化
    const handlePageChange = (page) => {
        setCurrentPage(page);
    };

    // 显示弹窗
    const showNewModal = () => {
        setIsAddRecordVisible(true);
        // 重置表单
        addForm.resetFields();
    };

    // 关闭弹窗
    const handleNewModelCancel = () => {
        setIsAddRecordVisible(false);
    };

    // 显示弹窗
    const showEditModal = (updatingRecord) => {
        setCurrentRecord(updatingRecord);
        // 关键：为表单赋值（字段名需与Form.Item的name属性一致）
        editForm.setFieldsValue({
            name: updatingRecord.name,
            mode: updatingRecord.type.toString(),
            remark: updatingRecord.remark || '', // 处理可能为undefined的情况
        });
        setIsEditRecordVisible(true);
    };

    // 关闭弹窗
    const handleEditModelCancel = () => {
        setIsEditRecordVisible(false);
    };


  // 提交新增表单
  const handleAddSubmit = async () => {
    try {
      // 1. 验证表单（同步操作，但返回 Promise，可使用 await）
      const values = await addForm.validateFields();
      console.log('表单验证通过:', values);
      let spFileArr =[];
      let knowledgeFileArr = [];
      for (let i = 0; i < values.spFiles.length; i++) {
        const element = values.spFiles[i];
        spFileArr.push(element.response.data.id);
      }
      if (values.knowledgeFiles !== undefined) {
        for (let i = 0; i < values.knowledgeFiles.length; i++) {
            const item = values.knowledgeFiles[i];
            knowledgeFileArr.push(item.response.data.id);
        }
      }
     
      const newRecord = {
        name: values.name,
        mode: values.mode,
        remark: values.remark,
        spFiles:spFileArr,
        knowledgeFiles:knowledgeFileArr
      };
      // 2. 请求新增接口
      message.loading('提交中...', 0); // 显示加载中提示
      // 使用 await 等待接口返回
      const res = await create(newRecord);
        // 3. 处理成功结果
        if (res.data.success) {
            // 显示成功消息
            messageApi.open({
                type: 'success',
                content:'项目新增成功！',
            });
            //重置表单
            addForm.resetFields();
            await loadRecord();
            // 关闭弹窗
            setIsAddRecordVisible(false);
        } else {
            messageApi.open({
                type: 'error',
                content: `${res.data.msg}`,
            });
        }   
    } catch (error) {
      // 5. 处理错误（表单验证失败或接口错误）
      console.error('提交失败:', error);
      // 区分表单验证错误和接口错误
      if (error.name === 'ValidateError') {
        messageApi.open({
            type: 'error',
            content: '表单填写有误，请检查！',
        });
      } else {
        messageApi.open({
            type: 'error',
            content: '提交失败，请稍后重试！',
        });
      }
    }
  };
  

  
  const normFile = (e) => {
    if (Array.isArray(e)) {
      return e;
    }
    return e?.fileList;
  };


   // 提交修改表单
  const handleEditSubmit = async () => {
    try {
      // 1. 验证表单（同步操作，但返回 Promise，可使用 await）
      const values = await editForm.validateFields();
      console.log('表单验证通过:', values);
      const editRecord = {
        id: currentRecord.id,
        name: values.name,
        mode: values.mode,
        remark: values.remark,
      };
      // 2. 请求修改接口
      message.loading('提交中...', 0); // 显示加载中提示
      // 使用 await 等待接口返回
      const res = await update(editRecord);

       console.log("result", res.data);
        // 3. 处理成功结果
        if (res.data.success) {
            // 显示成功消息
            messageApi.open({
                type: 'success',
                content:'项目修改成功！',
            });
            //重置表单
            editForm.resetFields();
            await loadRecord();
            // 关闭弹窗
            setIsEditRecordVisible(false);
        } else {
            messageApi.open({
                type: 'error',
                content:`${res.data.msg}`,
            });
        }   
    } catch (error) {
      // 5. 处理错误（表单验证失败或接口错误）
      console.error('提交失败:', error);
      // 区分表单验证错误和接口错误
      if (error.name === 'ValidateError') {
        messageApi.open({
            type: 'error',
            content:'表单填写有误，请检查',
        });
      } else {
        messageApi.open({
            type: 'error',
            content:'提交失败，请稍后重试！',
        });
      }
    }
  };
  
   const confirm = async (record, idx, e) => {
        try {
            setDeletingRowIndex(record.id);
            setTimeout(async() => {
                await deleteRecord(record.id);
                messageApi.open({
                    type: 'success',
                    content:'项目删除成功',
                });
                //加载新数据
                loadRecord();
                setDeletingRowIndex(null);
            }, 1000);
        } catch (error) {
            console.log("delete error:", error);
            messageApi.open({
                type: 'error',
                content:`${error}`,
            });
        }
   }

   //获取数据
   const fetchRecords = async (param) => {
       const result = await axios.post('http://8.134.218.222:7000/project/item/query', param)
       console.log("project fetch:", result);
       return result;
   }

    //新增项目
    const create = async (param) => {
        const result = await axios.post('http://8.134.218.222:7000/project/item/create', param)
        return result;
    }

    //修改项目
    const update = async (param) => {
        const result = await axios.post('http://8.134.218.222:7000/project/item/update', param)
        console.log("updating:", result);
        return result;
    }

    //删除项目
    const deleteRecord = async (id) => {
        const result = await axios.delete(`http://8.134.218.222:7000/project/item/delete/${id}`)
        return result;
    }

    return ( 
        <div>
            {contextHolder}
            <Spin spinning={isLoading} fullscreen tip="加载中..." />
            <h2>项目列表</h2>
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 }}>
                <Input 
                placeholder="请输入项目名称" 
                value={searchText} 
                onChange={(e) => setSearchText(e.target.value)} 
                style={{ marginRight: 8, width: 200 }}
                />
                <Button type="primary" onClick={handleSearch}>
                搜索
                </Button>
                <Button type="default" style={{ marginLeft: 8 }} onClick={showNewModal}>
                添加
               </Button>
            </div>
            <Table
                key='projects' 
                columns={columns} 
                // dataSource={filteredData.slice((currentPage - 1) * pageSize, currentPage * pageSize)} 
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
             {/* 表单弹窗 */}
            <Modal
                title="添加新项目"
                open={isAddRecordVisible}
                onCancel={handleNewModelCancel}
                footer={[
                <Button key="back" onClick={handleNewModelCancel}>
                    取消
                </Button>,
                <Button key="submit" type="primary" onClick={handleAddSubmit}>
                    保存
                </Button>,
                ]}
                destroyOnHidden={true}
                maskClosable={false}
                width={600}
            >
                <Form
                form={addForm}
                layout="vertical"
                name="project_form"
                initialValues={{ pendingReview: 'N' }}
                >
                <Form.Item
                    name="name"
                    label="项目名称"
                    rules={[{ required: true, message: '请输入项目名称!' }]}
                >
                    <Input placeholder="请输入项目名称" maxLength={100} />
                </Form.Item>
                <Form.Item
                    name="mode"
                    label="类型"
                    rules={[{ required: true, message: '请选择类型!' }]}
                >
                  <Radio.Group>
                    <Radio value="1"> 自动 </Radio>
                    <Radio value="2"> 手动 </Radio>
                  </Radio.Group>
                </Form.Item>
                <Form.Item
                    name="remark"
                    label="备注"
                >
                    <Input.TextArea rows={3} placeholder="请输入备注" />
                </Form.Item>
                <Form.Item
                    label="存储过程文件"
                    name="spFiles"
                    valuePropName="fileList"
                    getValueFromEvent={normFile}
                    rules={[{ required: true, message: '请上传存储过程文件' }]}
                >
                    <Upload name='file' action="http://8.134.218.222:7000/sys/file/upload" >
                     <Button icon={<UploadOutlined />}>Upload</Button>
                    </Upload>
                </Form.Item>
                <Form.Item
                    label="知识库文件"
                    name="knowledgeFiles"
                    valuePropName="fileList"
                    getValueFromEvent={normFile}
                   
                >
                    <Upload  multiple={true}>
                     <Button icon={<UploadOutlined />}>Upload</Button>
                    </Upload>
                </Form.Item>
                </Form>
            </Modal>
            <Modal
                title="修改项目信息"
                visible={isEditRecordVisible}
                onCancel={handleEditModelCancel}
                footer={[
                <Button key="back" onClick={handleEditModelCancel}>
                    取消
                </Button>,
                <Button key="submit" type="primary" onClick={handleEditSubmit}>
                    保存
                </Button>,
                ]}
                destroyOnClose={true}
                maskClosable={false}
                width={600}
            >
                <Form
                form={editForm}
                layout="vertical"
                name="project_edit_form"
                initialValues={{ pendingReview: 'N' }}
                >
                <Form.Item
                    name="name"
                    label="项目名称"
                >
                    <Input  disabled={true} maxLength={100} />
                </Form.Item>
                <Form.Item
                    name="mode"
                    label="类型"
                    rules={[{ required: true, message: '请选择类型!' }]}
                >
                  <Radio.Group>
                    <Radio value="1"> 自动 </Radio>
                    <Radio value="2"> 手动 </Radio>
                  </Radio.Group>
                </Form.Item>
                <Form.Item
                    name="remark"
                    label="备注"
                >
                    <Input.TextArea rows={3} placeholder="请输入备注" />
                </Form.Item>
                </Form>
            </Modal>
        </div>
    )
    
};

export default ProjectList;