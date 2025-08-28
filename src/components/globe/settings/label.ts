import type { GlobeInstance } from 'globe.gl';
import { BufferGeometry, Group, Line, LineBasicMaterial, Vector3 } from 'three';
import { renderPortTooltip } from '../../PortTooltipRenderer';
import _ from 'lodash';
import type { LocationQuantityInfo } from '../../../type';
import { greatCircleDistance } from './material';

export const setLabels = (globe: GlobeInstance, earthData: LocationQuantityInfo[]) => {
    const { labels, labelAltitudes } = convertLabelData(earthData);

    globe
        .labelsData(labels)
        .labelLat((obj: object) => {
            const it = obj as LocationQuantityInfo;
            return it.latitude;
        })
        .labelLng((obj: object) => {
            const it = obj as LocationQuantityInfo;
            return it.longitude;
        })
        .labelText((obj: object) => {
            const it = obj as LocationQuantityInfo;
            return `${it.location}\n(${it.quantity})`;
        })
        // Label size based on quantity
        .labelSize((obj: object) => {
            const it = obj as LocationQuantityInfo;
            return Math.max(0.8, Math.min(2.5, Math.sqrt(Math.max(it.quantity, 0)) * 0.08));
        })
        // Dot radius based on quantity
        .labelDotRadius((obj: object) => {
            const it = obj as LocationQuantityInfo;
            const minRadius = 0.15;
            const maxRadius = 0.8;
            const scale = Math.sqrt(Math.max(it.quantity, 0)) * 0.025;
            return Math.max(minRadius, Math.min(maxRadius, scale));
        })
        // Label color (use a single color for now)
        .labelColor(() => '#00ffe7')
        .labelResolution(8)
        .labelAltitude((obj: object) => {
            const it = obj as LocationQuantityInfo;
            return labelAltitudes[it.location] || 0;
        })
        .labelDotOrientation('top')
        .labelIncludeDot(true)
        // Custom HTML label tooltip using styled component
        .labelLabel((obj: object) => {
            const it = obj as LocationQuantityInfo;
            return renderPortTooltip({
                location: it.location,
                latitude: it.latitude,
                longitude: it.longitude,
                quantity: it.quantity,
            });
        })
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
