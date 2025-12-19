import { useState } from 'react';
import { reportService } from '../../services/api';

const ReportSection = ({ onResults, onError, onLoading, loading }) => {
  const [repoUrl, setRepoUrl] = useState('');
  const [branch, setBranch] = useState('main');
  const [path, setPath] = useState('');
  const [usePath, setUsePath] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleGenerateReport = async () => {
    if (isLoading) return;

    try {
      setIsLoading(true);
      onLoading(true);
      onError(null);
      
      let response;
      if (usePath && path.trim()) {
        // Use local path
        response = await reportService.techDebt(null, null, path.trim());
      } else if (repoUrl.trim()) {
        // Use GitHub repository
        response = await reportService.techDebt(repoUrl.trim(), branch || 'main', null);
      } else {
        onError('Please provide either a repository URL or a file path');
        setIsLoading(false);
        onLoading(false);
        return;
      }
      
      // Check if response has error
      if (response.data && response.data.status === 'error') {
        onError(response.data.error || 'An error occurred');
      } else {
        onResults(response.data);
      }
    } catch (err) {
      const errorMessage = err.response?.data?.error || 
                          err.response?.data?.message || 
                          err.message || 
                          'An error occurred';
      onError(errorMessage);
      console.error('Error generating report:', err);
    } finally {
      setIsLoading(false);
      onLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-br from-purple-50 via-pink-50 to-purple-100 rounded-xl p-6 space-y-4 border border-purple-200/50 shadow-lg">
        <div className="flex items-center space-x-3 mb-4 p-3 bg-white/60 rounded-lg border border-purple-200/50">
          <input
            type="checkbox"
            id="usePathReport"
            checked={usePath}
            onChange={(e) => setUsePath(e.target.checked)}
            className="w-5 h-5 rounded border-purple-300 text-purple-600 focus:ring-purple-500 focus:ring-2 cursor-pointer"
          />
          <label htmlFor="usePathReport" className="text-sm font-semibold text-black cursor-pointer flex-1">
            Use local file path instead of repository URL
          </label>
        </div>

        {usePath ? (
          <div>
            <label className="block text-sm font-semibold text-black mb-2">
              Code Path
            </label>
            <input
              type="text"
              value={path}
              onChange={(e) => setPath(e.target.value)}
              placeholder="/path/to/your/code"
              className="w-full px-4 py-3 border-2 border-purple-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-all duration-300 bg-white/80 shadow-sm hover:shadow-md text-black"
            />
            <p className="text-xs text-black/70 mt-2 ml-1">
              Path to the codebase for technical debt analysis
            </p>
          </div>
        ) : (
          <>
            <div>
              <label className="block text-sm font-semibold text-black mb-2">
                Repository URL
              </label>
              <input
                type="text"
                value={repoUrl}
                onChange={(e) => setRepoUrl(e.target.value)}
                placeholder="https://github.com/user/repo.git"
                className="w-full px-4 py-3 border-2 border-purple-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-all duration-300 bg-white/80 shadow-sm hover:shadow-md text-black"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-black mb-2">
                Branch (optional)
              </label>
              <input
                type="text"
                value={branch}
                onChange={(e) => setBranch(e.target.value)}
                placeholder="main"
                className="w-full px-4 py-3 border-2 border-purple-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-all duration-300 bg-white/80 shadow-sm hover:shadow-md text-black"
              />
            </div>
          </>
        )}
        <button
          onClick={handleGenerateReport}
          disabled={isLoading}
          className="relative w-full px-6 py-4 bg-gradient-to-r from-purple-600 via-pink-500 to-purple-500 text-white rounded-xl hover:shadow-xl disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-[1.02] font-bold text-lg shadow-lg overflow-hidden"
        >
          {isLoading ? (
            <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-r from-purple-600 via-pink-500 to-purple-500 z-10">
              <div className="flex items-center space-x-2">
                <div className="bright-loading-dot"></div>
                <div className="bright-loading-dot"></div>
                <div className="bright-loading-dot"></div>
                <div className="bright-loading-dot"></div>
                <div className="bright-loading-dot"></div>
              </div>
            </div>
          ) : (
            <span>Generate Tech Debt Report</span>
          )}
        </button>
      </div>

      <div className="bg-gradient-to-br from-purple-50 to-pink-50 border-2 border-purple-200 rounded-xl p-5 shadow-lg">
        <h4 className="font-bold text-black mb-3 text-lg">
          Technical Debt Report
        </h4>
        <p className="text-sm text-black/80 mb-3">
          Generate a comprehensive technical debt report from GitHub repositories or local paths. The report includes:
        </p>
        <ul className="text-sm text-black/80 space-y-2 list-disc list-inside">
          <li>Overall technical debt score</li>
          <li>Risk level assessment</li>
          <li>Detailed breakdown of issues</li>
          <li>Files scanned count</li>
          <li>Severity breakdown (High, Medium, Low)</li>
          <li>Recommendations for improvement</li>
        </ul>
      </div>
    </div>
  );
};

export default ReportSection;

