import { useState } from 'react';
import { aiService } from '../../services/api';

const AISection = ({ onResults, onError, onLoading, loading }) => {
  const [repoUrl, setRepoUrl] = useState('');
  const [branch, setBranch] = useState('main');
  const [path, setPath] = useState('');
  const [usePath, setUsePath] = useState(false);
  const [loadingOperation, setLoadingOperation] = useState(null);

  const handleSubmit = async (serviceMethod, operationName) => {
    if (loadingOperation) return;

    try {
      setLoadingOperation(operationName);
      onLoading(true);
      onError(null);

      let response;
      if (usePath && path.trim()) {
        response = await serviceMethod(null, null, path.trim());
      } else if (repoUrl.trim()) {
        response = await serviceMethod(repoUrl.trim(), branch || 'main', null);
      } else {
        onError('Please provide either a repository URL or a file path');
        setLoadingOperation(null);
        onLoading(false);
        return;
      }

      onResults(response.data);
    } catch (err) {
      onError(err.response?.data?.error || err.message || 'An error occurred');
    } finally {
      setLoadingOperation(null);
      onLoading(false);
    }
  };


  const operations = [
    {
      name: 'Code Insights',
      description: 'Get AI-powered insights about your codebase',
      method: aiService.codeInsights,
      gradient: 'from-purple-500 to-pink-500',
      bgGradient: 'from-purple-50 to-pink-50',
    },
    {
      name: 'Defect Prediction',
      description: 'Predict potential defects in your code',
      method: aiService.defectPrediction,
      gradient: 'from-red-500 to-orange-500',
      bgGradient: 'from-red-50 to-orange-50',
    },
    {
      name: 'Test Gap Analysis',
      description: 'Analyze gaps in test coverage',
      method: aiService.testGapAnalysis,
      gradient: 'from-blue-500 to-cyan-500',
      bgGradient: 'from-blue-50 to-cyan-50',
    },
    {
      name: 'Refactor Advisor',
      description: 'Get recommendations for code refactoring',
      method: aiService.refactorAdvisor,
      gradient: 'from-green-500 to-emerald-500',
      bgGradient: 'from-green-50 to-emerald-50',
    },
    {
      name: 'Memory Leak Prediction',
      description: 'Identify potential memory leaks',
      method: aiService.memoryLeakPrediction,
      gradient: 'from-indigo-500 to-purple-500',
      bgGradient: 'from-indigo-50 to-purple-50',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Enhanced Input Form */}
      <div className="bg-gradient-to-br from-pink-50 via-rose-50 to-pink-100 rounded-xl p-6 space-y-4 border border-pink-200/50 shadow-lg">
        <div className="flex items-center space-x-3 mb-4 p-3 bg-white/60 rounded-lg border border-pink-200/50">
          <input
            type="checkbox"
            id="usePath"
            checked={usePath}
            onChange={(e) => setUsePath(e.target.checked)}
            className="w-5 h-5 rounded border-pink-300 text-pink-600 focus:ring-pink-500 focus:ring-2 cursor-pointer"
          />
          <label htmlFor="usePath" className="text-sm font-semibold text-black cursor-pointer flex-1">
            Use local file path instead of repository URL
          </label>
        </div>

        {usePath ? (
          <div>
            <label className="block text-sm font-semibold text-black mb-2">
              File Path
            </label>
            <input
              type="text"
              value={path}
              onChange={(e) => setPath(e.target.value)}
              placeholder="/path/to/your/code"
              className="w-full px-4 py-3 border-2 border-pink-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-500 focus:border-pink-500 transition-all duration-300 bg-white/80 shadow-sm hover:shadow-md"
            />
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
                className="w-full px-4 py-3 border-2 border-pink-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-500 focus:border-pink-500 transition-all duration-300 bg-white/80 shadow-sm hover:shadow-md text-black"
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
                className="w-full px-4 py-3 border-2 border-pink-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-pink-500 focus:border-pink-500 transition-all duration-300 bg-white/80 shadow-sm hover:shadow-md text-black"
              />
            </div>
          </>
        )}
      </div>

      {/* Enhanced Operations */}
      <div className="space-y-4">
        <h3 className="text-xl font-bold text-black">
          AI Operations
        </h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {operations.map((op) => (
            <div
              key={op.name}
              className={`bg-gradient-to-br ${op.bgGradient} border-2 border-transparent hover:border-pink-300 rounded-xl p-5 transition-all duration-300 transform hover:scale-[1.02] hover:shadow-xl group flex flex-col`}
            >
              <div className="flex-1 mb-4">
                <h4 className="font-bold text-black text-lg mb-2">{op.name}</h4>
                <p className="text-sm text-black/80">{op.description}</p>
              </div>
              <button
                onClick={() => handleSubmit(op.method, op.name)}
                disabled={loadingOperation !== null}
                className={`relative w-full px-6 py-3 bg-gradient-to-r ${op.gradient} text-white rounded-xl hover:shadow-lg disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-105 font-semibold shadow-md overflow-hidden ${
                  loadingOperation === op.name ? 'pointer-events-none' : ''
                } ${loadingOperation !== null && loadingOperation !== op.name ? 'opacity-50' : ''}`}
              >
                {loadingOperation === op.name ? (
                  <div className={`absolute inset-0 flex items-center justify-center bg-gradient-to-r ${op.gradient} z-10`}>
                    <div className="flex items-center space-x-2">
                      <div className="bright-loading-dot"></div>
                      <div className="bright-loading-dot"></div>
                      <div className="bright-loading-dot"></div>
                      <div className="bright-loading-dot"></div>
                      <div className="bright-loading-dot"></div>
                    </div>
                  </div>
                ) : (
                  <span>Run</span>
                )}
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default AISection;

