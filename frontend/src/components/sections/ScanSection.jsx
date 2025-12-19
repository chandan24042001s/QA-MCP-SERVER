import { useState } from 'react';
import { scanService } from '../../services/api';

const ScanSection = ({ onResults, onError, onLoading, loading }) => {
  const [repoUrl, setRepoUrl] = useState('');
  const [branch, setBranch] = useState('main');
  const [path, setPath] = useState('');
  const [scanType, setScanType] = useState('repository'); // 'repository' or 'files'
  const [loadingButton, setLoadingButton] = useState(null);

  const handleScanRepository = async () => {
    if (loadingButton) return;

    if (!repoUrl.trim()) {
      onError('Please provide a repository URL');
      return;
    }

    try {
      setLoadingButton('repository');
      onLoading(true);
      onError(null);
      const response = await scanService.scanRepository(repoUrl.trim(), branch || 'main');
      onResults(response.data);
    } catch (err) {
      onError(err.response?.data?.error || err.message || 'An error occurred');
    } finally {
      setLoadingButton(null);
      onLoading(false);
    }
  };

  const handleScanFiles = async () => {
    if (loadingButton) return;

    if (!path.trim()) {
      onError('Please provide a file path');
      return;
    }

    try {
      setLoadingButton('files');
      onLoading(true);
      onError(null);
      const response = await scanService.scanFiles(path.trim());
      onResults(response.data);
    } catch (err) {
      onError(err.response?.data?.error || err.message || 'An error occurred');
    } finally {
      setLoadingButton(null);
      onLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-br from-blue-50 via-cyan-50 to-blue-100 rounded-xl p-6 space-y-4 border border-blue-200/50 shadow-lg">
        <div className="flex space-x-4 mb-4 bg-white/60 p-3 rounded-lg border border-blue-200/50">
          <label className="flex items-center space-x-3 cursor-pointer flex-1 p-3 rounded-lg hover:bg-white/80 transition-colors">
            <input
              type="radio"
              value="repository"
              checked={scanType === 'repository'}
              onChange={(e) => setScanType(e.target.value)}
              className="w-5 h-5 border-blue-300 text-blue-600 focus:ring-blue-500 focus:ring-2 cursor-pointer"
            />
            <div>
              <span className="text-sm font-semibold text-black">Scan Repository</span>
            </div>
          </label>
          <label className="flex items-center space-x-3 cursor-pointer flex-1 p-3 rounded-lg hover:bg-white/80 transition-colors">
            <input
              type="radio"
              value="files"
              checked={scanType === 'files'}
              onChange={(e) => setScanType(e.target.value)}
              className="w-5 h-5 border-blue-300 text-blue-600 focus:ring-blue-500 focus:ring-2 cursor-pointer"
            />
            <div>
              <span className="text-sm font-semibold text-black">Scan Files</span>
            </div>
          </label>
        </div>

        {scanType === 'repository' ? (
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
                className="w-full px-4 py-3 border-2 border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300 bg-white/80 shadow-sm hover:shadow-md text-black"
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
                className="w-full px-4 py-3 border-2 border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300 bg-white/80 shadow-sm hover:shadow-md text-black"
              />
            </div>
            <button
              onClick={handleScanRepository}
              disabled={loadingButton !== null}
              className="relative w-full px-6 py-4 bg-gradient-to-r from-blue-600 via-cyan-500 to-blue-500 text-white rounded-xl hover:shadow-xl disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-[1.02] font-bold text-lg shadow-lg overflow-hidden"
            >
              {loadingButton === 'repository' ? (
                <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-r from-blue-600 via-cyan-500 to-blue-500 z-10">
                  <div className="flex items-center space-x-2">
                    <div className="bright-loading-dot"></div>
                    <div className="bright-loading-dot"></div>
                    <div className="bright-loading-dot"></div>
                    <div className="bright-loading-dot"></div>
                    <div className="bright-loading-dot"></div>
                  </div>
                </div>
              ) : (
                <span>Scan Repository</span>
              )}
            </button>
          </>
        ) : (
          <>
            <div>
              <label className="block text-sm font-semibold text-black mb-2">
                File Path
              </label>
              <input
                type="text"
                value={path}
                onChange={(e) => setPath(e.target.value)}
                placeholder="/path/to/your/files"
                className="w-full px-4 py-3 border-2 border-blue-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-300 bg-white/80 shadow-sm hover:shadow-md text-black"
              />
            </div>
            <button
              onClick={handleScanFiles}
              disabled={loadingButton !== null}
              className="relative w-full px-6 py-4 bg-gradient-to-r from-blue-600 via-cyan-500 to-blue-500 text-white rounded-xl hover:shadow-xl disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-[1.02] font-bold text-lg shadow-lg overflow-hidden"
            >
              {loadingButton === 'files' ? (
                <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-r from-blue-600 via-cyan-500 to-blue-500 z-10">
                  <div className="flex items-center space-x-2">
                    <div className="bright-loading-dot"></div>
                    <div className="bright-loading-dot"></div>
                    <div className="bright-loading-dot"></div>
                    <div className="bright-loading-dot"></div>
                    <div className="bright-loading-dot"></div>
                  </div>
                </div>
              ) : (
                <span>Scan Files</span>
              )}
            </button>
          </>
        )}
      </div>

      <div className="bg-gradient-to-br from-blue-50 to-cyan-50 border-2 border-blue-200 rounded-xl p-5 shadow-lg">
        <h4 className="font-bold text-black mb-3 text-lg">
          What does scanning do?
        </h4>
        <ul className="text-sm text-black/80 space-y-2 list-disc list-inside">
          <li>Analyzes code for potential issues</li>
          <li>Calculates technical debt score</li>
          <li>Identifies severity levels (High, Medium, Low)</li>
          <li>Provides risk assessment</li>
        </ul>
      </div>
    </div>
  );
};

export default ScanSection;

