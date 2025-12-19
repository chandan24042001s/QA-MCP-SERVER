import { useState } from 'react';
import AISection from './sections/AISection';
import ScanSection from './sections/ScanSection';
import TestSection from './sections/TestSection';
import ReportSection from './sections/ReportSection';

const Dashboard = ({ onResults, onError, onLoading, loading }) => {
  const [activeTab, setActiveTab] = useState('ai');

  const tabs = [
    { id: 'ai', label: 'AI Analysis', color: 'from-purple-500 to-pink-500' },
    { id: 'scan', label: 'Code Scan', color: 'from-blue-500 to-cyan-500' },
    { id: 'test', label: 'Test Execution', color: 'from-green-500 to-emerald-500' },
    { id: 'report', label: 'Reports', color: 'from-orange-500 to-red-500' },
  ];

  return (
    <div className="bg-white/90 backdrop-blur-xl rounded-2xl shadow-2xl border border-pink-200/50 p-8 transform transition-all duration-300 hover:shadow-3xl">
      <div className="mb-8">
        <div className="flex items-center space-x-3 mb-6">
          <div className="w-10 h-10 bg-gradient-to-br from-pink-400 to-rose-500 rounded-xl flex items-center justify-center shadow-lg">
          </div>
          <h2 className="text-3xl font-bold text-black">
            Operations
          </h2>
        </div>
        
        {/* Enhanced Tabs */}
        <div className="flex space-x-3 bg-gradient-to-r from-pink-50 to-rose-50 p-2 rounded-xl border border-pink-200/50">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`relative px-6 py-3 font-semibold text-sm rounded-lg transition-all duration-300 transform hover:scale-105 ${
                activeTab === tab.id
                  ? `bg-gradient-to-r ${tab.color} text-white shadow-lg scale-105`
                  : 'text-black hover:bg-white/60'
              }`}
            >
              {tab.label}
              {activeTab === tab.id && (
                <div className="absolute -bottom-1 left-1/2 transform -translate-x-1/2 w-2 h-2 bg-white rounded-full animate-pulse"></div>
              )}
            </button>
          ))}
        </div>
      </div>

      {/* Tab Content with fade animation */}
      <div className="mt-8 animate-fadeIn">
        {activeTab === 'ai' && (
          <AISection
            onResults={onResults}
            onError={onError}
            onLoading={onLoading}
            loading={loading}
          />
        )}
        {activeTab === 'scan' && (
          <ScanSection
            onResults={onResults}
            onError={onError}
            onLoading={onLoading}
            loading={loading}
          />
        )}
        {activeTab === 'test' && (
          <TestSection
            onResults={onResults}
            onError={onError}
            onLoading={onLoading}
            loading={loading}
          />
        )}
        {activeTab === 'report' && (
          <ReportSection
            onResults={onResults}
            onError={onError}
            onLoading={onLoading}
            loading={loading}
          />
        )}
      </div>
    </div>
  );
};

export default Dashboard;

