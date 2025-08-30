import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import MainLayout from './MainLayout';
import Home from './pages/Home';
import ProjectList from './pages/ProjectList';
import ProjectDetail from './pages/ProjectDetail';
import TaskList from './pages/TaskList';
import TaskDetail from './pages/TaskDetail';
import FileDetail from './pages/FileDetail';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Home />} />
          <Route path="project-list" element={<ProjectList />} />
          <Route path="project-detail/:id" element={<ProjectDetail />} />
          <Route path="task-list" element={<TaskList />} />
          <Route path="task-detail/:id" element={<TaskDetail />} />
          <Route path="file-detail/:id" element={<FileDetail />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;