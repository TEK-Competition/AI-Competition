'use client'
import Image from "next/image";
import { useRef, useState } from 'react';
import { createWorker, PSM, createScheduler } from 'tesseract.js';
// import cv from 'opencv.js'
import styles from "./page.module.css";

export default function Home() {
  // method one : use 阿里api.
  const queryData = async(_data: any) => {
    try {
      const data = await fetch('https://dashscope.aliyuncs.com/api/v1/apps/5ec6f795a4884adfa3bbb7f8f860dc96/completion', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer sk-7d51bdd255db481eb9367dc503ef5a17',
            'Content-Type': 'application/json'
          },
        body: JSON.stringify({
            input: {
              prompt: _data,
            }
          })
      })

      const result = await data.json(); 
      return result;

    } catch(e) {
      console.log('===========>e', e);
    }

  }

  const [text, setText] = useState<string>('');
  const [progress, setProgress] = useState<number>(0);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const canvasRef = useRef(null);
  const [jsonData, setJsonData] = useState([]);
  

  const downloadtoCSV = (initData) => {
    let result = {};
    // 扁平化处理函数
    const flattenObject = (obj, prefix = '') => {
      for (const key in obj) {
        if (typeof obj[key] === 'object' && obj[key] !== null && !Array.isArray(obj[key])) {
          const flattened = flattenObject(obj[key], `${prefix}${key}.`);
          result = {...result, ...flattened};
        } else if (Array.isArray(obj[key])) {
          // 处理商品数组
          obj[key].forEach((item, index) => {
            const itemFlattened = flattenObject(item, `${prefix}${key}[${index}].`);
            result = {...result, ...itemFlattened};
          });
        } else {
          result[`${prefix}${key}`] = obj[key];
        }
      }
      return result;
    }

    // 将数据转换为CSV格式
    const convertToCSV = (data: object) => {
      const flattenedData = flattenObject(data);
      const headers = Object.keys(flattenedData);

      // CSV头部s
      let csv = headers.map(header => `"${header}"`).join(',') + '\n';
      // CSV 数据行
      const row = headers.map(header => {
        const value = flattenedData[header] !== undefined ? flattenedData[header] : '';
        // 处理值中的逗号和引号
        return typeof value === 'string' ? `"${value.replace(/"/g, '""')}"` : value;
      });
      csv += row.join(',') + '\n';
      return csv;
    }

    const content = convertToCSV(jsonData);
    console.log('=========>content', content);
    const blob = new Blob([content], {type: 'text/csv;charset=utf-8'});
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    const data = new Date().toISOString().slice(0, 10);
    link.setAttribute('href', url);
    link.setAttribute('download', `发票数据-${data}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

  }

  const handleImageUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) { return; };
    setIsLoading(true);
    setProgress(0);
    try {
      // const processeUrl = await lightweightProcess(file);
      const scheduler = createScheduler();
      const worker1 = await createWorker('chi_sim');
      // const worker2 = await createWorker('chi_sim');
      scheduler.addWorker(worker1);
      // scheduler.addWorker(worker2);
      await worker1.setParameters({
        tessedit_pageseg_mode: PSM.AUTO_OSD,
        // tessedit_char_whitelist: '1234567890/-:.年月日',
      })
      const [data1, data2] = await Promise.all([scheduler.addJob('recognize', file)]);
      const data = `${data1.data.text}`;
      setText(data); 
      const result = await queryData(data);
      setJsonData(JSON.parse(result.output.text))
      await scheduler.terminate();
      setIsLoading(false)
    } catch(error) {
      console.log('OCR Error: ', error);
    } 
  }


  return (
    <div className={styles.page}>
      <h1>上传图片自动提取表格数据</h1>
      <div>
        <input 
          type="file"
          accept="image/*"
          onChange={handleImageUpload}
          disabled={isLoading}
        />
        <button onClick={downloadtoCSV} disabled={isLoading}>下载  </button>
      </div>
     

      {text && (
        <div>
          <h2>识别结果</h2>
          <pre>{text}</pre>
        </div>
      )}
      <canvas ref={canvasRef} style={{ display: 'none' }}/>
    </div>
  );
}