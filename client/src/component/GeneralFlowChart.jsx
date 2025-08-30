import React, { useMemo } from'react';
import { Card, ConfigProvider } from 'antd';
// import ReactFlow, { 
//   MiniMap, 
//   Controls, 
//   Background,
//   useNodesState,
//   useEdgesState
// } from'react-flow-renderer';
// import 'react-flow-renderer/dist/style.css';
import ReactFlow, {
    MiniMap,
    Controls,
    Background,
    useNodesState,
    useEdgesState,
    addEdge,
  } from 'reactflow';
  import 'reactflow/dist/style.css';
  
  

/**
 * 通用流程图组件
 * @param {Object} props 
 * @param {Array} props.data - 流程图数据，格式为[{from: string, to: string}]
 * @param {string} props.title - 流程图标题
 * @param {Object} props.nodePositions - 节点位置配置，可选
 * @param {Object} props.nodeStyles - 节点样式配置，可选
 * @param {Object} props.edgeOptions - 连接线配置，可选
 * @param {number} props.height - 流程图高度，默认700
 * @param {boolean} props.draggable - 是否允许拖拽节点，默认true
 * @param {boolean} props.showMiniMap - 是否显示迷你地图，默认true
 */
const GeneralFlowChart = ({
  key,
  data,
  title = '流程图',
  nodePositions = {},
  nodeStyles = {},
  edgeOptions = {},
  height = 700,
  draggable = true,
  showMiniMap = true,
}) => {
  // 提取所有唯一节点
  const nodesSet = useMemo(() => {
    if (!Array.isArray(data)) return [];
    
    const set = new Set();
    data.forEach(link => {
      if (link.from) set.add(link.from);
      if (link.to) set.add(link.to);
    });
    return Array.from(set);
  }, [data]);

  // 生成节点默认位置（如果没有提供）
  const getDefaultPosition = (nodeId, index, totalNodes) => {
    // 如果已有位置配置，使用配置的位置
    if (nodePositions[nodeId]) {
      return nodePositions[nodeId];
    }
    
    // 自动布局算法：按层次分布节点
    const columns = 4; // 分为4列
    const columnWidth = 250;
    const rowHeight = 100;
    const columnIndex = index % columns;
    const rowIndex = Math.floor(index / columns);
    
    return {
      x: 100 + columnIndex * columnWidth,
      y: 100 + rowIndex * rowHeight
    };
  };

  // 获取节点样式
  const getNodeStyle = (nodeId) => {
    // 默认样式
    const defaultStyles = {
      regular: {
        background: '#fff',
        borderColor: '#1890ff',
        color: '#000',
        padding: '8px 12px',
        borderRadius: '4px'
      },
      temp: { // 临时节点（带#前缀）
        background: '#fffbe6',
        borderColor: '#faad14',
        color: '#faad14'
      },
      end: { // 终结节点（NONE）
        background: '#fff1f0',
        borderColor: '#f5222d',
        color: '#f5222d'
      }
    };
    
    // 确定节点类型
    let styleType = 'regular';
    if (nodeId.startsWith('#')) styleType = 'temp';
    if (nodeId === 'NONE') styleType = 'end';
    
    
    // 合并默认样式和用户自定义样式
    return {
     ...defaultStyles[styleType],
     ...(nodeStyles[styleType] || {}),
     ...(nodeStyles[nodeId] || {}), // 针对特定节点的样式
      width: 'auto',
      minWidth: 80
    };
  };

  // 创建节点数据
  const initialNodes = useMemo(() => {
    return nodesSet.map((nodeId, index) => ({
      id: nodeId,
      type: 'default',
      data: { 
        label: nodeStyles[nodeId]?.label || nodeId.replace('#', '') 
      },
      position: getDefaultPosition(nodeId, index, nodesSet.length),
      style: getNodeStyle(nodeId)
    }));
  }, [nodesSet, nodePositions, nodeStyles]);

  // 创建连接线数据
  const initialEdges = useMemo(() => {
    if (!Array.isArray(data)) return [];
    
    return data.map((link, index) => ({
      id: `edge-${index}`,
      source: link.from,
      target: link.to,
      animated: true,
      arrowHeadType: 'arrowclosed',
     ...edgeOptions // 应用用户自定义的连接线配置
    }));
  }, [data, edgeOptions]);

  // 初始化节点和连接线状态
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  // 处理节点拖拽后的位置更新
  const handleNodeDragStop = (event, node) => {
    setNodes(prevNodes => 
      prevNodes.map(n => 
        n.id === node.id? {...n, position: node.position } : n
      )
    );
  };

  return (
    // <ConfigProvider>
      
    // </ConfigProvider>
    <Card title={title} style={{ margin: 24, overflow: 'hidden' }}>
        <div style={{ height, width: '100%' }} key={key}>
          <ReactFlow
            nodes={nodes}
            edges={edges}
            onNodesChange={onNodesChange}
            onEdgesChange={onEdgesChange}
            onNodeDragStop={handleNodeDragStop}
            nodesDraggable={draggable}
            nodesConnectable={false}
            zoomOnScroll={true}
            defaultZoom={0.8}
          >
            <Background variant="dots" gap={100} size={2} />
            <Controls />
            {showMiniMap && <MiniMap />}
          </ReactFlow>
        </div>
      </Card>
  );
};

export default GeneralFlowChart;
    