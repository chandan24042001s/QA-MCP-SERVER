import ResultDisplay from './ResultDisplay';

const ResultsPanel = ({ results, error, loading }) => {
  return (
    <div className="bg-white/90 backdrop-blur-xl rounded-2xl shadow-2xl border border-pink-200/50 p-8 sticky top-8 transform transition-all duration-300 hover:shadow-3xl w-full">
      <div className="flex items-center space-x-3 mb-6">
        <div className="w-10 h-10 bg-gradient-to-br from-pink-400 to-rose-500 rounded-xl flex items-center justify-center shadow-lg">
        </div>
        <h2 className="text-3xl font-bold text-black">
          Results
        </h2>
      </div>
      
      {loading && (
        <div className="flex flex-col items-center justify-center py-16 space-y-6">
          <div className="flex items-center space-x-3">
            <div className="bright-loading-dot"></div>
            <div className="bright-loading-dot"></div>
            <div className="bright-loading-dot"></div>
            <div className="bright-loading-dot"></div>
            <div className="bright-loading-dot"></div>
          </div>
          <div className="text-center">
            <p className="text-lg font-semibold text-black animate-pulse">Processing...</p>
            <p className="text-sm text-black/70 mt-1">Please wait while we analyze</p>
          </div>
        </div>
      )}

      {error && (
        <div className="bg-gradient-to-br from-red-50 to-rose-50 border-2 border-red-200 rounded-xl p-6 shadow-lg transform hover:scale-[1.02] transition-transform duration-300">
          <div className="flex items-start space-x-4">
            <div className="w-12 h-12 bg-gradient-to-br from-red-400 to-rose-500 rounded-xl flex items-center justify-center shadow-lg flex-shrink-0">
            </div>
            <div className="flex-1">
              <h3 className="text-black font-bold text-lg mb-2">Error Occurred</h3>
              <p className="text-black bg-white/50 rounded-lg p-3 border border-red-200">{error}</p>
            </div>
          </div>
        </div>
      )}

      {!loading && !error && !results && (
        <div className="text-center py-16">
          <div className="w-24 h-24 mx-auto mb-6 bg-gradient-to-br from-pink-100 to-rose-100 rounded-full flex items-center justify-center shadow-lg animate-pulse">
          </div>
          <p className="text-xl font-semibold text-black mb-2">No results yet</p>
          <p className="text-sm text-black/70">Select an operation to see results here</p>
          <div className="mt-6 flex justify-center space-x-2">
            <div className="w-2 h-2 bg-pink-400 rounded-full animate-bounce" style={{ animationDelay: '0s' }}></div>
            <div className="w-2 h-2 bg-pink-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
            <div className="w-2 h-2 bg-pink-400 rounded-full animate-bounce" style={{ animationDelay: '0.4s' }}></div>
          </div>
        </div>
      )}

      {!loading && !error && results && (
        <div className="animate-fadeIn">
          <ResultDisplay data={results} />
        </div>
      )}
    </div>
  );
};

export default ResultsPanel;

