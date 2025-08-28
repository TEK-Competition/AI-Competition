import { useEffect, useCallback } from 'react';
import type { GlobeInstance } from 'globe.gl';

interface KeyboardShortcutsProps {
  globe: GlobeInstance | null;
  isEnabled?: boolean;
  onShortcutTriggered?: (shortcut: string, action: string) => void;
  tourControlRef?: React.RefObject<{ startDemoWithId: (demoId?: string) => void }>;
  onAISummaryToggle?: () => void;
}

interface ShortcutAction {
  key: string;
  description: string;
  action: () => void;
  category: 'navigation' | 'demo' | 'data' | 'view';
}

export const useKeyboardShortcuts = ({
  globe,
  isEnabled = true,
  onShortcutTriggered,
  tourControlRef,
  onAISummaryToggle
}: KeyboardShortcutsProps) => {

  // Predefined camera positions for quick navigation
  const cameraPositions = {
    global: { lat: 20, lng: 0, altitude: 2.5 },
    asia: { lat: 35, lng: 120, altitude: 1.5 },
    europe: { lat: 50, lng: 10, altitude: 1.8 },
    northAmerica: { lat: 39, lng: -98, altitude: 1.5 },
    middleEast: { lat: 25, lng: 45, altitude: 1.6 },
    southAmerica: { lat: -15, lng: -60, altitude: 1.8 }
  };

  // Camera transition function
  const transitionToPosition = useCallback((position: any) => {
    if (!globe) return;
    globe.pointOfView(position, 1500);
    onShortcutTriggered?.('camera', `Moving to ${JSON.stringify(position)}`);
  }, [globe, onShortcutTriggered]);

  // Define all keyboard shortcuts
  const shortcuts: ShortcutAction[] = [
    // Navigation shortcuts
    {
      key: '1',
      description: 'Global Overview',
      category: 'navigation',
      action: () => transitionToPosition(cameraPositions.global)
    },
    {
      key: '2',
      description: 'Asia-Pacific Region',
      category: 'navigation',
      action: () => transitionToPosition(cameraPositions.asia)
    },
    {
      key: '3',
      description: 'European Region',
      category: 'navigation',
      action: () => transitionToPosition(cameraPositions.europe)
    },
    {
      key: '4',
      description: 'North America',
      category: 'navigation',
      action: () => transitionToPosition(cameraPositions.northAmerica)
    },
    {
      key: '5',
      description: 'Middle East',
      category: 'navigation',
      action: () => transitionToPosition(cameraPositions.middleEast)
    },
    {
      key: '6',
      description: 'South America',
      category: 'navigation',
      action: () => transitionToPosition(cameraPositions.southAmerica)
    },

    // View control shortcuts
    {
      key: 'm',
      description: 'Toggle Auto-Rotation',
      category: 'view',
      action: () => {
        if (!globe) return;
        const controls = globe.controls();
        controls.autoRotate = !controls.autoRotate;
        onShortcutTriggered?.('view', `Auto-rotation ${controls.autoRotate ? 'enabled' : 'disabled'}`);
      }
    },
    {
      key: 'z',
      description: 'Zoom to Fit',
      category: 'view',
      action: () => {
        if (!globe) return;
        globe.pointOfView({ altitude: 2.5 }, 1000);
        onShortcutTriggered?.('view', 'Zoom to fit');
      }
    },
    {
      key: 'f',
      description: 'Focus Mode (Closer View)',
      category: 'view',
      action: () => {
        if (!globe) return;
        const currentPov = globe.pointOfView();
        globe.pointOfView({ ...currentPov, altitude: 1.2 }, 1000);
        onShortcutTriggered?.('view', 'Focus mode activated');
      }
    },

    // Demo shortcuts
    {
      key: 'q',
      description: 'Quick Overview Demo',
      category: 'demo',
      action: () => {
        if (tourControlRef?.current) {
          tourControlRef.current.startDemoWithId('quick-overview');
          onShortcutTriggered?.('demo', 'Quick Overview');
        }
      }
    },
    {
      key: 'w',
      description: 'Detailed Analysis Demo',
      category: 'demo',
      action: () => {
        if (tourControlRef?.current) {
          tourControlRef.current.startDemoWithId('detailed-analysis');
          onShortcutTriggered?.('demo', 'Detailed Analysis');
        }
      }
    },
    {
      key: 'e',
      description: 'Regional Deep Dive Demo',
      category: 'demo',
      action: () => {
        if (tourControlRef?.current) {
          tourControlRef.current.startDemoWithId('regional-deep-dive');
          onShortcutTriggered?.('demo', 'Regional Deep Dive');
        }
      }
    },
    {
      key: 'a',
      description: 'Toggle AI Summary Chat',
      category: 'view',
      action: () => {
        if (onAISummaryToggle) {
          onAISummaryToggle();
          onShortcutTriggered?.('view', 'AI Summary Chat toggled');
        }
      }
    },

    // Speed controls
    {
      key: '+',
      description: 'Increase Rotation Speed',
      category: 'view',
      action: () => {
        if (!globe) return;
        const controls = globe.controls();
        controls.autoRotateSpeed = Math.min(controls.autoRotateSpeed + 0.1, 2.0);
        onShortcutTriggered?.('view', `Rotation speed: ${controls.autoRotateSpeed.toFixed(1)}`);
      }
    },
    {
      key: '-',
      description: 'Decrease Rotation Speed',
      category: 'view',
      action: () => {
        if (!globe) return;
        const controls = globe.controls();
        controls.autoRotateSpeed = Math.max(controls.autoRotateSpeed - 0.1, 0.1);
        onShortcutTriggered?.('view', `Rotation speed: ${controls.autoRotateSpeed.toFixed(1)}`);
      }
    },

    // Reset and utility
    {
      key: '0',
      description: 'Reset to Default View',
      category: 'navigation',
      action: () => {
        if (!globe) return;
        globe.pointOfView({ lat: 39.6, lng: -98.5, altitude: 2 }, 2000);
        const controls = globe.controls();
        controls.autoRotate = true;
        controls.autoRotateSpeed = 0.3;
        onShortcutTriggered?.('navigation', 'Reset to default view');
      }
    },
    {
      key: 'i',
      description: 'Zoom In Animation (From Far to Initial)',
      category: 'view',
      action: () => {
        if (!globe) return;
        // First move camera to a very far position (Earth appears as a small dot)
        globe.pointOfView({ lat: 39.6, lng: -98.5, altitude: 32 }, 500);
        
        // Then animate to initial position after a short delay
        setTimeout(() => {
          globe.pointOfView({ lat: 39.6, lng: -98.5, altitude: 2 }, 2500);
        }, 600);
        
        onShortcutTriggered?.('view', '');
      }
    }
  ];

  // Handle keydown events
  const handleKeyDown = useCallback((event: KeyboardEvent) => {
    if (!isEnabled || !globe) return;

    // Ignore if user is typing in an input field
    const target = event.target as HTMLElement;
    if (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.contentEditable === 'true') {
      return;
    }

    // Prevent default for our shortcuts
    const shortcut = shortcuts.find(s => s.key.toLowerCase() === event.key.toLowerCase());
    if (shortcut) {
      event.preventDefault();
      shortcut.action();
    }
  }, [isEnabled, globe, shortcuts, onShortcutTriggered, tourControlRef, onAISummaryToggle]);

  // Setup event listeners
  useEffect(() => {
    if (!isEnabled) return;

    document.addEventListener('keydown', handleKeyDown);
    
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isEnabled, handleKeyDown]);

  // Return the shortcuts for display purposes
  return {
    shortcuts,
    cameraPositions,
    transitionToPosition
  };
};

// Component to display available shortcuts
export const ShortcutsHelp: React.FC<{ 
  shortcuts: ShortcutAction[]; 
  isVisible: boolean;
  onClose: () => void;
}> = ({ shortcuts, isVisible, onClose }) => {
  if (!isVisible) return null;

  const groupedShortcuts = shortcuts.reduce((groups, shortcut) => {
    const category = shortcut.category;
    if (!groups[category]) {
      groups[category] = [];
    }
    groups[category].push(shortcut);
    return groups;
  }, {} as Record<string, ShortcutAction[]>);

  return (
    <div
      style={{
        position: 'fixed',
        top: '50%',
        left: '50%',
        transform: 'translate(-50%, -50%)',
        zIndex: 1002,
        background: 'linear-gradient(135deg, rgba(20, 25, 30, 0.98), rgba(30, 35, 40, 0.98))',
        backdropFilter: 'blur(20px)',
        border: '1px solid rgba(77, 208, 225, 0.3)',
        borderRadius: '16px',
        padding: '24px',
        maxWidth: '600px',
        maxHeight: '80vh',
        overflowY: 'auto',
        boxShadow: '0 20px 60px rgba(0, 0, 0, 0.5)'
      }}
    >
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: '20px'
      }}>
        <h2 style={{ 
          color: '#4dd0e1', 
          margin: 0, 
          fontSize: '20px',
          fontWeight: '600'
        }}>
          Keyboard Shortcuts
        </h2>
        <button
          onClick={onClose}
          style={{
            background: 'none',
            border: 'none',
            color: '#78909c',
            fontSize: '24px',
            cursor: 'pointer',
            padding: '4px'
          }}
        >
          ×
        </button>
      </div>

      {Object.entries(groupedShortcuts).map(([category, categoryShortcuts]) => (
        <div key={category} style={{ marginBottom: '20px' }}>
          <h3 style={{ 
            color: '#26c6da', 
            fontSize: '14px', 
            marginBottom: '12px',
            textTransform: 'uppercase',
            letterSpacing: '1px',
            fontWeight: '500'
          }}>
            {category.replace(/([A-Z])/g, ' $1').toUpperCase()}
          </h3>
          
          {categoryShortcuts.map((shortcut, index) => (
            <div
              key={index}
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '8px 0',
                borderBottom: index < categoryShortcuts.length - 1 ? '1px solid rgba(77, 208, 225, 0.1)' : 'none'
              }}
            >
              <span style={{ color: '#b0bec5', fontSize: '14px' }}>
                {shortcut.description}
              </span>
              <kbd style={{
                background: 'rgba(77, 208, 225, 0.15)',
                border: '1px solid rgba(77, 208, 225, 0.3)',
                borderRadius: '4px',
                padding: '4px 8px',
                color: '#4dd0e1',
                fontSize: '12px',
                fontFamily: 'monospace',
                fontWeight: 'bold'
              }}>
                {shortcut.key.toUpperCase()}
              </kbd>
            </div>
          ))}
        </div>
      ))}
      
      <div style={{ 
        marginTop: '20px', 
        padding: '12px', 
        background: 'rgba(77, 208, 225, 0.1)', 
        borderRadius: '8px',
        fontSize: '12px',
        color: '#78909c'
      }}>
        <strong>Tip:</strong> These shortcuts work when you're not typing in input fields. 
        Press <kbd style={{ 
          background: 'rgba(77, 208, 225, 0.2)', 
          padding: '2px 4px', 
          borderRadius: '2px' 
        }}>H</kbd> to toggle this help.
      </div>
    </div>
  );
};
