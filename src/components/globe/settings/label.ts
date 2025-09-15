import type { GlobeInstance } from 'globe.gl';
import { BufferGeometry, Group, Line, LineBasicMaterial, Vector3 } from 'three';
import { renderPortTooltip } from '../../PortTooltipRenderer';
import _ from 'lodash';
import type { LocationQuantityInfo } from '../../../type';
import { greatCircleDistance } from './material';

export const setLabels = (globe: GlobeInstance, earthData: LocationQuantityInfo[]) => {
    const { labels, labelAltitudes } = convertLabelData(earthData);

    // 使用HTML元素替代文本标签，以更好地支持中文
    globe
        .htmlElementsData(labels)
        .htmlLat((obj: object) => {
            const it = obj as LocationQuantityInfo;
            return it.latitude;
        })
        .htmlLng((obj: object) => {
            const it = obj as LocationQuantityInfo;
            return it.longitude;
        })
        .htmlElement((obj: object) => {
            const it = obj as LocationQuantityInfo;
            const el = document.createElement('div');
            
            // 创建点标记
            const dot = document.createElement('div');
            const dotRadius = Math.max(3, Math.min(8, Math.sqrt(Math.max(it.quantity, 0)) * 0.5));
            dot.style.width = dotRadius * 2 + 'px';
            dot.style.height = dotRadius * 2 + 'px';
            dot.style.backgroundColor = '#00ffe7';
            dot.style.borderRadius = '50%';
            dot.style.margin = '0 auto 5px auto';
            dot.style.boxShadow = '0 0 5px rgba(0, 255, 231, 0.5)';
            
            // 创建文本标签
            const label = document.createElement('div');
            label.innerHTML = `${it.location}<br/>(${it.quantity})`;
            label.style.color = '#00ffe7';
            label.style.fontFamily = '"PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "WenQuanYi Micro Hei", "Arial", "Helvetica", sans-serif';
            label.style.fontSize = Math.max(10, Math.min(16, Math.sqrt(Math.max(it.quantity, 0)) * 0.5)) + 'px';
            label.style.fontWeight = '700';
            label.style.textAlign = 'center';
            label.style.textShadow = '0 0 1px rgba(0, 0, 0, 1), 0 0 3px rgba(0, 0, 0, 0.8), 0 0 5px rgba(0, 255, 231, 0.3)';
            label.style.webkitTextStroke = '0.5px rgba(0, 0, 0, 0.8)';
            label.style.whiteSpace = 'nowrap';
            
            el.appendChild(dot);
            el.appendChild(label);
            el.style.pointerEvents = 'auto'; // 启用鼠标事件以支持tooltip
            el.style.userSelect = 'none';
            el.style.display = 'flex';
            el.style.flexDirection = 'column';
            el.style.alignItems = 'center';
            el.style.cursor = 'pointer'; // 添加鼠标指针样式
            
            // 添加tooltip功能
            let tooltipDiv: HTMLDivElement | null = null;
            let updatePosition: ((event: MouseEvent) => void) | null = null;
            
            el.addEventListener('mouseenter', (e) => {
                // 创建tooltip
                tooltipDiv = document.createElement('div');
                tooltipDiv.innerHTML = renderPortTooltip({
                    location: it.location,
                    latitude: it.latitude,
                    longitude: it.longitude,
                    quantity: it.quantity,
                });
                tooltipDiv.style.position = 'absolute';
                tooltipDiv.style.pointerEvents = 'none';
                tooltipDiv.style.zIndex = '10000';
                tooltipDiv.style.transform = 'translate(10px, -50%)';
                document.body.appendChild(tooltipDiv);
                
                // 更新tooltip位置
                updatePosition = (event: MouseEvent) => {
                    if (tooltipDiv) {
                        tooltipDiv.style.left = event.clientX + 'px';
                        tooltipDiv.style.top = event.clientY + 'px';
                    }
                };
                
                updatePosition(e as any);
                el.addEventListener('mousemove', updatePosition);
            });
            
            el.addEventListener('mouseleave', () => {
                if (tooltipDiv) {
                    document.body.removeChild(tooltipDiv);
                    tooltipDiv = null;
                }
                if (updatePosition) {
                    el.removeEventListener('mousemove', updatePosition);
                    updatePosition = null;
                }
            });
            
            return el;
        })
        .htmlAltitude((obj: object) => {
            const it = obj as LocationQuantityInfo;
            return labelAltitudes[it.location] || 0;
        })
        .htmlTransitionDuration(0);

    // 保留原来的标签系统作为备用（设置为空数据）
    globe
        .labelsData([])
        .labelsTransitionDuration(0);

    // --- BEGIN: Add vertical lines from earth surface to label dots ---
    let labelLinesGroup: Group | null = null;
    if (globe.scene().getObjectByName('labelLinesGroup')) {
        labelLinesGroup = globe.scene().getObjectByName('labelLinesGroup') as Group;
        globe.scene().remove(labelLinesGroup);
    }
    labelLinesGroup = new Group();
    labelLinesGroup.name = 'labelLinesGroup';
    const RADIUS = globe.getGlobeRadius ? globe.getGlobeRadius() : 1; // fallback to 1 if not available
    // build points from labels (labels are aggregated entries with from & quantity)
    type LabelPoint = { port: string; lat: number; lng: number };

    const labelPoints: LabelPoint[] = labels.map((d) => ({
        port: d.location,
        lat: d.latitude,
        lng: d.longitude,
    }));

    labelPoints.forEach((d: LabelPoint) => {
        const alt = labelAltitudes[d.port] || 0;
        if (alt > 0) {
            // Convert lat/lng to 3D positions
            const latRad = (d.lat * Math.PI) / 180;
            const lngRad = (d.lng * Math.PI) / 180;
            // Surface point
            const r0 = RADIUS;
            const x0 = r0 * Math.cos(latRad) * Math.sin(lngRad);
            const y0 = r0 * Math.sin(latRad);
            const z0 = r0 * Math.cos(latRad) * Math.cos(lngRad);
            // Label dot point (globe.gl altitude is relative to radius)
            const r1 = RADIUS * (1 + alt);
            const x1 = r1 * Math.cos(latRad) * Math.sin(lngRad);
            const y1 = r1 * Math.sin(latRad);
            const z1 = r1 * Math.cos(latRad) * Math.cos(lngRad);
            // Create geometry
            const geometry = new BufferGeometry().setFromPoints([new Vector3(x0, y0, z0), new Vector3(x1, y1, z1)]);
            const material = new LineBasicMaterial({
                color: 0xffa500,
                linewidth: 2,
            });
            const line = new Line(geometry, material);
            labelLinesGroup.add(line);
        }
    });
    globe.scene().add(labelLinesGroup);
};

const LABEL_BASE_ALT = 0;
const LABEL_STEP_ALT = 0.1;
const LABEL_DIST_THRESHOLD = 4;
const LABEL_MAX_LAYER = 4;

export const convertLabelData = (locations: LocationQuantityInfo[]) => {
    const labelAltitudes: Record<string, number> = {};

    // sort locations by quantity desc for stable ordering
    const sorted = [...locations].sort((a, b) => b.quantity - a.quantity);

    for (let i = 0; i < sorted.length; i++) {
        const p = sorted[i];
        let layer = 0;
        for (let j = 0; j < i; j++) {
            const q = sorted[j];
            const dist =
                (greatCircleDistance({ lat: p.latitude, lng: p.longitude }, { lat: q.latitude, lng: q.longitude }) *
                    180) /
                Math.PI;
            if (dist < LABEL_DIST_THRESHOLD && labelAltitudes[q.location] / LABEL_STEP_ALT === layer) {
                layer++;
                if (layer > LABEL_MAX_LAYER) break;
            }
        }
        labelAltitudes[p.location] = layer === 0 ? 0 : LABEL_BASE_ALT + layer * LABEL_STEP_ALT;
    }

    // labels are simply the sorted LocationQuantityInfo entries
    const labels = sorted;

    return {
        labelAltitudes,
        labels,
    };
};
