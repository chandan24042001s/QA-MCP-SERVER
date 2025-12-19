import { useState } from 'react';
import Dashboard from './components/Dashboard';
import ResultsPanel from './components/ResultsPanel';

function App() {
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleResults = (data) => {
    setResults(data);
    setError(null);
  };

  const handleError = (err) => {
    setError(err);
    setResults(null);
  };

  const handleLoading = (isLoading) => {
    setLoading(isLoading);
  };

  return (
    <div className="min-h-screen relative overflow-hidden">
      {/* Pinkish gradient background with z-index layers */}
      <div className="fixed inset-0 z-0">
        <div className="absolute inset-0 bg-gradient-to-br from-pink-100 via-rose-100 to-pink-200"></div>
        <div className="absolute inset-0 bg-gradient-to-tr from-pink-200/40 via-rose-200/30 to-pink-100/40 animate-float"></div>
        <div className="absolute top-0 left-1/4 w-96 h-96 bg-pink-300/30 rounded-full blur-3xl animate-pulse-glow"></div>
        <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-rose-300/30 rounded-full blur-3xl animate-pulse-glow" style={{ animationDelay: '1.5s' }}></div>
        <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-pink-200/20 rounded-full blur-3xl"></div>
        <div className="absolute top-1/4 right-1/4 w-72 h-72 bg-rose-200/25 rounded-full blur-3xl"></div>
        <div className="absolute bottom-1/4 left-1/4 w-80 h-80 bg-pink-300/20 rounded-full blur-3xl"></div>
      </div>

      {/* Content with higher z-index */}
      <div className="relative z-10">
        {/* Header */}
        <header className="bg-white/80 backdrop-blur-xl shadow-lg border-b border-pink-200/50 sticky top-0 z-20">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            <div className="flex items-center space-x-4">
              <div className="w-12 h-12 bg-gradient-to-br from-pink-400 to-rose-500 rounded-xl flex items-center justify-center shadow-lg transform hover:scale-110 transition-transform duration-300">
              </div>
              <div>
                <h1 className="text-4xl font-bold text-black">
                  QA MCP Server Dashboard
                </h1>
                <p className="text-sm text-black/70 mt-1 font-medium">
                  Quality Assurance Analysis and Testing Tools
                </p>
              </div>
            </div>
          </div>
        </header>

        {/* Main Content */}
        <main className="max-w-[1800px] mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            <div className="lg:col-span-1">
              <Dashboard
                onResults={handleResults}
                onError={handleError}
                onLoading={handleLoading}
                loading={loading}
              />
            </div>
            <div className="lg:col-span-1">
              <ResultsPanel
                results={results}
                error={error}
                loading={loading}
              />
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}

export default App;

