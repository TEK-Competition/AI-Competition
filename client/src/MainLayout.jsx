import React from 'react';
import { Layout, Menu, Avatar } from 'antd';
import { useNavigate, Outlet } from 'react-router-dom';
import {
  HomeOutlined,
  AppstoreOutlined,
  DatabaseOutlined,

} from '@ant-design/icons';

const { Sider, Content } = Layout;

const MainLayout = () => {
  const navigate = useNavigate();

  const menuItems = [
    { key: 'home', label: '首页', icon: <HomeOutlined />, path: '/' },
    {
      key: 'project',
      label: '项目管理',
      icon: <AppstoreOutlined />,
      children: [
        {
          key: 'project-list',
          label: '项目列表',
          path: 'project-list',
        },
      ],
    },
    {
      key: 'task',
      label: '任务管理',
      icon: <DatabaseOutlined />,
      children: [
        { key: 'task-list', label: '任务列表', path: 'task-list' },
      ],
    },
  ];

  const handleMenuClick = ({ key, item }) => {
    const menuItem = menuItems.find((item) => item.key === key) || 
                     menuItems.flatMap((item) => item.children || []).find((child) => child.key === key);
    if (menuItem && menuItem.path) {
      navigate(menuItem.path);
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider theme="dark" width={220}>
        <div style={{ height: 64, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ color: 'white', fontSize: 18, fontWeight: 'bold' }}>Code Looms</div>
        </div>
        <Menu
          defaultSelectedKeys={['home']}
          mode="inline"
          onClick={handleMenuClick}
          items={menuItems.map((item) => ({
            key: item.key,
            label: item.label,
            icon: item.icon,
            children: item.children?.map((child) => ({
              key: child.key,
              label: child.label,
              path: child.path,
            })),
          }))}
        />
      </Sider>
      <Layout>
        <div style={{ padding: '0 24px', height: 64, display: 'flex', alignItems: 'center', justifyContent: 'flex-end', backgroundColor:'black' }}>
          {/* <Avatar style={{ marginRight: 8 }}></Avatar>
          <span style={{ color: 'white' }}></span> */}
        </div>
        <Content style={{background: '#fff', minHeight: 280, padding: 24 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;