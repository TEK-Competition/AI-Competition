import { useEffect, useRef } from 'react';
import type { GlobeInstance } from 'globe.gl';
import { initGlobe } from './settings/init';
import { setMaterial } from './settings/material';
import { setLabels } from './settings/label';
import { applyControls } from './settings/control';
import { useAppSelector } from '../../redux/hook';
import { styled } from 'styled-components';
import { useKeyboardShortcuts, ShortcutsHelp } from '../../hooks/useKeyboardShortcuts';
import type { LocationInfo, LocationQuantityInfo } from '../../type';

// Arc datum type for globe arcs
interface ArcDatum {
    startLat: number;
    startLng: number;
    endLat: number;
    endLng: number;
    color: string | string[];
    altitude: number;
    stroke: number;
    quantity: number;
}

const GlobeContainer = styled.div`
    width: 100%;
    height: 100%;
    overflow: hidden;
    transition: transform 0.3s ease-in-out;

    .float-tooltip-kap {
        z-index: 2;
    }
    canvas {
        z-index: 1;
    }
`;

export const GlobeEarth = (props: {
    isPortSidebarCollapsed: boolean;
    showHelp?: boolean;
    onHelpToggle?: () => void;
    onAISummaryToggle?: () => void;
}) => {
    const refContainer = useRef<HTMLDivElement>(null);
    const refGlobe = useRef<GlobeInstance | null>(null);

    const currentYear = useAppSelector((state) => state.year.currentYear);
    const currentViewMode = useAppSelector((state) => state.view.mode);

    const earthData = useAppSelector((state) => state.loader.data?.earthData);
    const selectedLocations = useAppSelector((state) => state.ports.selectedLocations);
    console.log(`selectedLocations===`,selectedLocations);

    const setGlobe = () => {
        const globe = refGlobe.current!;
        if (!earthData || !currentYear) {
            globe.arcsData([]).labelsData([]);
            return;
        }

        if (!earthData) {
            setLabels(globe, []);
            return;
        }

        const yearItems = earthData.filter((it) => {
            if (!it?.date) return false;
            const d = new Date(it.date);
            if (Number.isNaN(d.getTime())) return false;
            return d.getFullYear() === Number(currentYear);
        });

        // 如果有选中的港口，仅保留包含选中港口的条目（from 或 to 在 selectedLocations 中）
        const filteredItems =
            selectedLocations && selectedLocations.length > 0
                ? yearItems.filter((it) => {
                      const fromName = it.from?.location;
                      const toName = it.to?.location;
                      return (
                          (fromName && selectedLocations.includes(fromName)) ||
                          (toName && selectedLocations.includes(toName))
                      );
                  })
                : yearItems;

        // aggregate totals by city (use from and to locations)
        const map: Record<string, LocationQuantityInfo> = {};
        filteredItems.forEach((it) => {
            const qty = it.quantity ?? 0;
            const add = (loc: LocationInfo) => {
                if (!loc || !loc.location) return;
                const location = loc.location;
                if (!map[location])
                    map[location] = { location, latitude: loc.latitude, longitude: loc.longitude, quantity: 0 };
                map[location].quantity += qty;
            };
            add(it.from);
            add(it.to);
        });

        const labelsData = Object.values(map);
        
        // 根据视图模式决定显示什么
        switch (currentViewMode) {
            case 'points':
                // 只显示点图（标签），不显示连线
                setLabels(globe, labelsData);
                globe.arcsData([]);
                break;
            case 'arcs':
                // 显示点和连线（从出发地飞向目的地的线）
                setLabels(globe, labelsData);
                setArcsData(globe, filteredItems);
                break;
            case 'all':
            default:
                // 显示所有（点图 + 连线）
                setLabels(globe, labelsData);
                setArcsData(globe, filteredItems);
                break;
        }
    };

    const setArcsData = (globe: GlobeInstance, filteredItems: any[]) => {
        // --- build arcs data from filteredItems ---
        // skip items without valid from/to coords
        const validArcs = filteredItems.filter((it) => it.from && it.to && it.from.latitude != null && it.from.longitude != null && it.to.latitude != null && it.to.longitude != null);
        const maxQty = Math.max(1, ...validArcs.map((it) => Math.max(0, it.quantity ?? 0)));

        const arcs: ArcDatum[] = validArcs.map((it) => {
            const qty = Math.max(0, it.quantity ?? 0);
            const norm = qty / maxQty;

            // compute central angle (radians) between two points on unit sphere
            const toRad = (deg: number) => (deg * Math.PI) / 180;
            const lat1 = toRad(it.from!.latitude);
            const lng1 = toRad(it.from!.longitude);
            const lat2 = toRad(it.to!.latitude);
            const lng2 = toRad(it.to!.longitude);
            const cosC = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1);
            const central = Math.max(0, Math.min(Math.PI, Math.acos(cosC)));

            // altitude increases with central angle so long routes arc higher over globe
            const baseAlt = 0.03; // minimal altitude
            const distanceFactor = (central / Math.PI) * 0.6; // up to 0.6 for antipodal
            const altitude = Math.min(0.9, baseAlt + distanceFactor + norm * 0.08);

            // slimmer strokes and scale with quantity
            const stroke = Math.max(0.12, Math.min(1.6, 0.25 + norm * 1.1));

            // more muted semi-transparent colors
            const startColor = 'rgba(0,100,90,0.7)';
            const endColor = 'rgba(200,120,80,0.65)';

            return {
                startLat: it.from!.latitude,
                startLng: it.from!.longitude,
                endLat: it.to!.latitude,
                endLng: it.to!.longitude,
                color: [startColor, endColor],
                altitude,
                stroke,
                quantity: qty,
            };
        });

        // apply arcs to globe with common callbacks
        globe
            .arcsData(arcs)
            .arcStartLat((obj: object) => {
                const d = obj as ArcDatum;
                return d.startLat;
            })
            .arcStartLng((obj: object) => {
                const d = obj as ArcDatum;
                return d.startLng;
            })
            .arcEndLat((obj: object) => {
                const d = obj as ArcDatum;
                return d.endLat;
            })
            .arcEndLng((obj: object) => {
                const d = obj as ArcDatum;
                return d.endLng;
            })
            .arcColor((obj: object) => {
                const d = obj as ArcDatum;
                return d.color;
            })
            .arcAltitude((obj: object) => {
                const d = obj as ArcDatum;
                return d.altitude;
            })
            .arcStroke((obj: object) => {
                const d = obj as ArcDatum;
                return d.stroke;
            })
            .arcDashLength(0.18)
            .arcDashGap(0.12)
            .arcDashInitialGap(() => Math.random())
            .arcDashAnimateTime(4000);
    };

    useEffect(() => {
        refGlobe.current = initGlobe(refContainer.current!);
        const timer = setMaterial(refGlobe.current);
        applyControls(refGlobe.current, refContainer.current!);
        setGlobe();
        return () => {
            clearInterval(timer);
        };
    }, []);

    useEffect(() => {
        setGlobe();
    }, [earthData, currentYear, selectedLocations, currentViewMode]);

    useEffect(() => {
        if (refGlobe.current && refContainer.current) {
            const container = refContainer.current;
            const containerWidth = container.clientWidth;

            // Calculate horizontal offset based on both sidebars
            let translateX = 0;

            // Adjust for port sidebar (right side)
            if (!props.isPortSidebarCollapsed) {
                translateX += containerWidth * 0.1; // Move right when port sidebar is expanded
            }

            // Apply the calculated transform to center the globe
            container.style.transform = `translateX(${translateX}px)`;
        }
    }, [props.isPortSidebarCollapsed]);

    // Setup keyboard shortcuts
    const { shortcuts } = useKeyboardShortcuts({
        globe: refGlobe.current,
        isEnabled: true,
        onAISummaryToggle: props.onAISummaryToggle,
    });

    // Handle help toggle with H key
    useEffect(() => {
        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.key.toLowerCase() === 'h' && !event.ctrlKey && !event.metaKey) {
                const target = event.target as HTMLElement;
                if (target.tagName !== 'INPUT' && target.tagName !== 'TEXTAREA') {
                    event.preventDefault();
                    props.onHelpToggle?.();
                }
            }
        };

        document.addEventListener('keydown', handleKeyDown);
        return () => document.removeEventListener('keydown', handleKeyDown);
    }, [props.onHelpToggle]);

    return (
        <>
            <GlobeContainer ref={refContainer} />

            <ShortcutsHelp
                shortcuts={shortcuts}
                isVisible={props.showHelp || false}
                onClose={() => props.onHelpToggle?.()}
            />
        </>
    );
};