const ResultDisplay = ({ data }) => {
  const renderValue = (key, value) => {
    if (value === null || value === undefined) {
      return (
        <span className="px-3 py-1 bg-gray-100 text-gray-500 rounded-lg text-sm font-mono">
          null
        </span>
      );
    }

    if (typeof value === 'object') {
      if (Array.isArray(value)) {
        return (
          <div className="ml-4 mt-2 space-y-2">
            {value.map((item, idx) => (
              <div key={idx} className="p-3 bg-gradient-to-br from-pink-50 to-rose-50 rounded-lg border border-pink-200/50 shadow-sm hover:shadow-md transition-shadow">
                {typeof item === 'object' ? (
                  <ResultDisplay data={item} />
                ) : (
                  <span className="text-black font-medium">{String(item)}</span>
                )}
              </div>
            ))}
          </div>
        );
      } else {
        return (
          <div className="ml-4 mt-2 border-l-3 border-pink-300 pl-4 bg-gradient-to-r from-pink-50/50 to-transparent rounded-r-lg py-2">
            <ResultDisplay data={value} />
          </div>
        );
      }
    }

    if (typeof value === 'boolean') {
      return (
        <span className={`px-4 py-2 rounded-lg text-sm font-bold shadow-sm ${
          value 
            ? 'bg-gradient-to-r from-green-400 to-emerald-500 text-white' 
            : 'bg-gradient-to-r from-red-400 to-rose-500 text-white'
        }`}>
          {String(value)}
        </span>
      );
    }

    if (typeof value === 'number') {
      return (
        <span className="px-4 py-2 bg-gradient-to-r from-blue-400 to-cyan-500 text-white rounded-lg font-bold shadow-md">
          {value}
        </span>
      );
    }

    // Special formatting for certain keys
    if (key === 'status') {
      const statusConfig = {
        completed: { gradient: 'from-green-400 to-emerald-500' },
        error: { gradient: 'from-red-400 to-rose-500' },
        running: { gradient: 'from-yellow-400 to-orange-500' },
        pending: { gradient: 'from-gray-400 to-gray-500' },
      };
      const config = statusConfig[value] || { gradient: 'from-gray-400 to-gray-500' };
      return (
        <span className={`px-4 py-2 bg-gradient-to-r ${config.gradient} text-white rounded-xl text-sm font-bold shadow-lg w-fit`}>
          {value}
        </span>
      );
    }

    if (key === 'riskLevel') {
      const riskConfig = {
        high: { gradient: 'from-red-500 to-rose-600' },
        medium: { gradient: 'from-yellow-500 to-orange-500' },
        low: { gradient: 'from-green-500 to-emerald-600' },
      };
      const config = riskConfig[value?.toLowerCase()] || { gradient: 'from-gray-400 to-gray-500' };
      return (
        <span className={`px-4 py-2 bg-gradient-to-r ${config.gradient} text-white rounded-xl text-sm font-bold shadow-lg w-fit`}>
          <span className="uppercase">{value}</span>
        </span>
      );
    }

    // Long text values or special keys that need full width
    if (typeof value === 'string' && (value.length > 50 || 
        key.toLowerCase().includes('reasoning') || 
        key.toLowerCase().includes('description') || 
        key.toLowerCase().includes('insight') ||
        key.toLowerCase().includes('recommendation') ||
        key.toLowerCase().includes('analysis') ||
        key.toLowerCase().includes('summary') ||
        key.toLowerCase().includes('finding') ||
        key.toLowerCase().includes('suggestion') ||
        key.toLowerCase().includes('explanation'))) {
      return (
        <div className="w-full bg-gradient-to-br from-pink-50 to-rose-50 p-5 rounded-xl border-2 border-pink-200/50 shadow-md mt-2">
          <div className="text-sm text-black whitespace-pre-wrap break-words leading-relaxed font-sans max-w-full">
            {value}
          </div>
        </div>
      );
    }

    return (
      <span className="px-3 py-1.5 bg-white border border-pink-200 rounded-lg text-black font-medium shadow-sm">
        {String(value)}
      </span>
    );
  };

  const formatKey = (key) => {
    return key
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, (str) => str.toUpperCase())
      .trim();
  };

  return (
    <div className="space-y-4 max-h-[700px] overflow-y-auto pr-2 w-full overflow-x-hidden">
      {Object.entries(data).map(([key, value], index) => {
        const isLongText = typeof value === 'string' && (
          value.length > 100 || 
          key.toLowerCase().includes('reasoning') || 
          key.toLowerCase().includes('description') || 
          key.toLowerCase().includes('insight') ||
          key.toLowerCase().includes('recommendation') ||
          key.toLowerCase().includes('analysis') ||
          key.toLowerCase().includes('summary')
        );
        
        return (
          <div 
            key={key} 
            className="bg-gradient-to-br from-white to-pink-50/30 rounded-xl p-5 border border-pink-200/50 shadow-md hover:shadow-xl transition-all duration-300 transform hover:scale-[1.01] w-full"
            style={{ animationDelay: `${index * 50}ms` }}
          >
            <div className={`flex items-start gap-4 ${isLongText ? 'flex-col' : ''} w-full`}>
              <div className="w-8 h-8 bg-gradient-to-br from-pink-400 to-rose-500 rounded-lg flex items-center justify-center shadow-md flex-shrink-0 mt-0.5">
                <span className="text-white text-xs font-bold">{index + 1}</span>
              </div>
              <div className="flex-1 w-full min-w-0 overflow-hidden">
                <div className="font-bold text-black mb-3 text-lg">
                  {formatKey(key)}
                </div>
                <div className="text-sm mt-1 w-full break-words">
                  {renderValue(key, value)}
                </div>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default ResultDisplay;

