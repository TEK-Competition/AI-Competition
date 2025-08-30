export const getTaskTypeValue = (typeId) => {
    let type_txt = ''
    switch (typeId) {
        case 1:
            type_txt = '自动';
            break;
        case 2:
            type_txt = '手动';
            break;
        default:
            type_txt = '';
            break;
    }
    return type_txt;
  }
  
  
export const getTaskStatusValue = (statusId) => {
    let status_txt = ''
    switch (statusId) {
        case 1:
            status_txt = '新建';
            break;
        case 2:
            status_txt = '运行';
            break;
        case 3:
            status_txt = '完成';
            break;  
        case 4:
            status_txt = '失败';
            break; 
        default:
            status_txt = '';
            break;
    }
    return status_txt;
  }
  
export  const getTaskStepsValue = (stepId) => {
    let step_txt = ''
    switch (stepId) {
        case 1:
            step_txt = '初始化';
            break;
        case 2:
            step_txt = '表关系任务';
            break;
        case 3:
            step_txt = '表字段任务';
            break;  
        case 4:
            step_txt = '转换任务';
            break;
        case 5:
            step_txt = '注释任务';
            break;  
        case 6:
            step_txt = '完成';
            break; 
        default:
            step_txt = '';
            break;
    }
    return step_txt;
  }



export const getProjectTypeValue = (typeId) => {
    let typeName = '';
    switch (typeId) {
      case 1:
          typeName = '自动';
          break;
      case 2:
          typeName = '手动';
          break;
      default:
          typeName = '';
          break;
   }
  return typeName;
}

export const getProjectStatusValue = (statusId) => {
  let statusName = ''
  switch (statusId) {
      case 1:
        statusName = '新建';
        break;
      case 2:
        statusName = '运行';
        break;
      case 3:
        statusName = '完成';
        break;  
      case 4:
        statusName = '失败';
        break; 
      default:
        statusName = '';
        break;
  }
  return statusName;
}


export const getStepStatus = (statusId) => {
    let status_txt = ''
    switch (statusId) {
        case 1:
            status_txt = 'wait';
            break;
        case 2:
            status_txt = 'process';
            break;
        case 3:
            status_txt = 'finish';
            break;  
        case 4:
            status_txt = 'error';
            break; 
        default:
            status_txt = 'wait';
            break;
    }
    return status_txt;
}

export const stages = [
   {'label':'初始化', 'value': '1' },
   {'label':'表关系任务', 'value':'2' },
   {'label':'表字段任务', 'value':'3' },
   {'label':'转换任务', 'value':'4' },
   {'label':'注释任务', 'value':'5' }
]


