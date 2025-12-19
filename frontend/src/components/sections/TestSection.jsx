import { useState } from 'react';
import { testService } from '../../services/api';

const TestSection = ({ onResults, onError, onLoading, loading }) => {
  const [path, setPath] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleRunTests = async () => {
    if (isLoading) return;

    if (!path.trim()) {
      onError('Please provide a test path');
      return;
    }

    try {
      setIsLoading(true);
      onLoading(true);
      onError(null);
      const response = await testService.runTests(path.trim());
      onResults(response.data);
    } catch (err) {
      onError(err.response?.data?.error || err.message || 'An error occurred');
    } finally {
      setIsLoading(false);
      onLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-gradient-to-br from-green-50 via-emerald-50 to-green-100 rounded-xl p-6 space-y-4 border border-green-200/50 shadow-lg">
        <div>
          <label className="block text-sm font-semibold text-black mb-2">
            Test Path
          </label>
          <input
            type="text"
            value={path}
            onChange={(e) => setPath(e.target.value)}
            placeholder="/path/to/test/directory"
            className="w-full px-4 py-3 border-2 border-green-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-all duration-300 bg-white/80 shadow-sm hover:shadow-md text-black"
          />
          <p className="text-xs text-black/70 mt-2 ml-1">
            Path to the directory containing your tests
          </p>
        </div>
        <button
          onClick={handleRunTests}
          disabled={isLoading}
          className="relative w-full px-6 py-4 bg-gradient-to-r from-green-600 via-emerald-500 to-green-500 text-white rounded-xl hover:shadow-xl disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-[1.02] font-bold text-lg shadow-lg overflow-hidden"
        >
          {isLoading ? (
            <div className="absolute inset-0 flex items-center justify-center bg-gradient-to-r from-green-600 via-emerald-500 to-green-500 z-10">
              <div className="flex items-center space-x-2">
                <div className="bright-loading-dot"></div>
                <div className="bright-loading-dot"></div>
                <div className="bright-loading-dot"></div>
                <div className="bright-loading-dot"></div>
                <div className="bright-loading-dot"></div>
              </div>
            </div>
          ) : (
            <span>Run Tests</span>
          )}
        </button>
      </div>

      <div className="bg-gradient-to-br from-green-50 to-emerald-50 border-2 border-green-200 rounded-xl p-5 shadow-lg">
        <h4 className="font-bold text-black mb-3 text-lg">
          Test Execution
        </h4>
        <p className="text-sm text-black/80 leading-relaxed">
          Execute your test suite and view the results. The system will run all tests
          in the specified directory and provide detailed output including pass/fail
          status, execution time, and any errors encountered.
        </p>
      </div>
    </div>
  );
};

export default TestSection;

