import { useEffect } from 'react';

const ResultsModal = ({ isOpen, onClose, results, error, loading }) => {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  // Extract findings from results data structure
  const extractFindings = (data) => {
    if (!data || typeof data !== 'object') return [];
    
    const findings = [];
    
    // Handle different possible data structures
    const processData = (obj) => {
      // If data has insights array
      if (Array.isArray(obj.insights)) {
        obj.insights.forEach((insight) => {
          if (Array.isArray(insight.findings)) {
            insight.findings.forEach((finding) => {
              findings.push({
                bugType: finding.type || finding.bugType || 'N/A',
                severity: finding.severity || 'N/A',
                lineNumber: finding.line || finding.lineNumber || 'N/A',
                evidence: finding.evidence || 'N/A',
                reason: finding.reasoning || finding.reason || 'N/A'
              });
            });
          }
        });
      }
      
      // If data has findings array directly
      if (Array.isArray(obj.findings)) {
        obj.findings.forEach((finding) => {
          findings.push({
            bugType: finding.type || finding.bugType || 'N/A',
            severity: finding.severity || 'N/A',
            lineNumber: finding.line || finding.lineNumber || 'N/A',
            evidence: finding.evidence || 'N/A',
            reason: finding.reasoning || finding.reason || 'N/A'
          });
        });
      }
      
      // If data has files array with findings
      if (Array.isArray(obj.files)) {
        obj.files.forEach((file) => {
          if (Array.isArray(file.findings)) {
            file.findings.forEach((finding) => {
              findings.push({
                bugType: finding.type || finding.bugType || 'N/A',
                severity: finding.severity || 'N/A',
                lineNumber: finding.line || finding.lineNumber || 'N/A',
                evidence: finding.evidence || 'N/A',
                reason: finding.reasoning || finding.reason || 'N/A'
              });
            });
          }
        });
      }
      
      // Recursively search for findings in nested objects
      Object.values(obj).forEach((value) => {
        if (value && typeof value === 'object' && !Array.isArray(value)) {
          processData(value);
        } else if (Array.isArray(value)) {
          value.forEach((item) => {
            if (item && typeof item === 'object') {
              processData(item);
            }
          });
        }
      });
    };
    
    processData(data);
    return findings;
  };

  const getStatus = (data) => {
    if (!data || typeof data !== 'object') return null;
    return data.status || data.Status || null;
  };

  const getTotalFiles = (data) => {
    if (!data || typeof data !== 'object') return null;
    return data.totalFilesAnalyzed || data.totalFiles || null;
  };

  const formatSeverity = (severity) => {
    const severityUpper = String(severity).toUpperCase();
    const severityConfig = {
      HIGH: { gradient: 'from-red-500 to-rose-600', text: 'HIGH' },
      MEDIUM: { gradient: 'from-yellow-500 to-orange-500', text: 'MEDIUM' },
      LOW: { gradient: 'from-green-500 to-emerald-600', text: 'LOW' },
    };
    const config = severityConfig[severityUpper] || { gradient: 'from-gray-400 to-gray-500', text: severityUpper };
    return (
      <span className={`px-3 py-1 bg-gradient-to-r ${config.gradient} text-white rounded-lg text-xs font-bold uppercase`}>
        {config.text}
      </span>
    );
  };

  const formatLineNumber = (line) => {
    if (line === 'N/A' || line === null || line === undefined) {
      return <span className="text-gray-500">N/A</span>;
    }
    return (
      <span className="px-3 py-1 bg-gradient-to-r from-blue-400 to-cyan-500 text-white rounded-lg text-sm font-semibold">
        {line}
      </span>
    );
  };

  const findings = results ? extractFindings(results) : [];
  const status = results ? getStatus(results) : null;
  const totalFiles = results ? getTotalFiles(results) : null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={onClose}
      ></div>
      
      {/* Modal */}
      <div className="relative bg-white/95 backdrop-blur-xl rounded-2xl shadow-2xl border border-pink-200/50 w-full max-w-6xl max-h-[90vh] flex flex-col animate-fadeIn">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-pink-200/50">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 bg-gradient-to-br from-pink-400 to-rose-500 rounded-xl flex items-center justify-center shadow-lg">
            </div>
            <h2 className="text-3xl font-bold text-black">
              Results
            </h2>
          </div>
          <button
            onClick={onClose}
            className="w-10 h-10 bg-gradient-to-br from-pink-400 to-rose-500 rounded-xl flex items-center justify-center shadow-lg hover:scale-110 transition-transform duration-300 text-white font-bold"
          >
            ×
          </button>
        </div>

        {/* Status Section at Top */}
        {!loading && !error && results && (
          <div className="px-6 pt-6 pb-4 border-b border-pink-200/50">
            <div className="flex flex-wrap items-center gap-4">
              {status && (
                <div className="flex items-center space-x-2">
                  <span className="text-sm font-semibold text-black">Status:</span>
                  <span className={`px-4 py-2 bg-gradient-to-r ${
                    status.toLowerCase() === 'completed' 
                      ? 'from-green-400 to-emerald-500' 
                      : status.toLowerCase() === 'error'
                      ? 'from-red-400 to-rose-500'
                      : 'from-yellow-400 to-orange-500'
                  } text-white rounded-xl text-sm font-bold shadow-lg`}>
                    {status}
                  </span>
                </div>
              )}
              {totalFiles !== null && (
                <div className="flex items-center space-x-2">
                  <span className="text-sm font-semibold text-black">Total Files Analyzed:</span>
                  <span className="px-4 py-2 bg-gradient-to-r from-blue-400 to-cyan-500 text-white rounded-xl text-sm font-bold shadow-lg">
                    {totalFiles}
                  </span>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
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
            <div className="bg-gradient-to-br from-red-50 to-rose-50 border-2 border-red-200 rounded-xl p-6 shadow-lg">
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

          {!loading && !error && results && findings.length > 0 && (
            <div className="overflow-x-auto">
              <table className="w-full border-collapse">
                <thead>
                  <tr className="bg-gradient-to-r from-pink-50 to-rose-50 border-b-2 border-pink-200">
                    <th className="px-4 py-4 text-left text-sm font-bold text-black uppercase tracking-wider">
                      SR.No
                    </th>
                    <th className="px-4 py-4 text-left text-sm font-bold text-black uppercase tracking-wider">
                      Bug Type
                    </th>
                    <th className="px-4 py-4 text-left text-sm font-bold text-black uppercase tracking-wider">
                      Severity
                    </th>
                    <th className="px-4 py-4 text-left text-sm font-bold text-black uppercase tracking-wider">
                      Line Number
                    </th>
                    <th className="px-4 py-4 text-left text-sm font-bold text-black uppercase tracking-wider">
                      Evidence
                    </th>
                    <th className="px-4 py-4 text-left text-sm font-bold text-black uppercase tracking-wider">
                      Reason
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-pink-100">
                  {findings.map((finding, index) => (
                    <tr 
                      key={index} 
                      className="hover:bg-gradient-to-r hover:from-pink-50/50 hover:to-rose-50/50 transition-colors duration-200"
                    >
                      <td className="px-4 py-4 whitespace-nowrap">
                        <span className="font-semibold text-black">{index + 1}</span>
                      </td>
                      <td className="px-4 py-4">
                        <span className="font-medium text-black">{finding.bugType}</span>
                      </td>
                      <td className="px-4 py-4 whitespace-nowrap">
                        {formatSeverity(finding.severity)}
                      </td>
                      <td className="px-4 py-4 whitespace-nowrap">
                        {formatLineNumber(finding.lineNumber)}
                      </td>
                      <td className="px-4 py-4">
                        <div className="max-w-md break-words">
                          {String(finding.evidence).length > 100 ? (
                            <div className="space-y-2">
                              <div className="text-black text-sm">{String(finding.evidence).substring(0, 100)}...</div>
                              <details className="cursor-pointer">
                                <summary className="text-pink-600 hover:text-pink-700 font-semibold text-xs">
                                  Show full evidence
                                </summary>
                                <div className="mt-2 p-3 bg-pink-50 rounded-lg border border-pink-200 text-black text-sm whitespace-pre-wrap">
                                  {finding.evidence}
                                </div>
                              </details>
                            </div>
                          ) : (
                            <span className="text-black text-sm">{finding.evidence}</span>
                          )}
                        </div>
                      </td>
                      <td className="px-4 py-4">
                        <div className="max-w-lg break-words">
                          {String(finding.reason).length > 150 ? (
                            <div className="space-y-2">
                              <div className="text-black text-sm">{String(finding.reason).substring(0, 150)}...</div>
                              <details className="cursor-pointer">
                                <summary className="text-pink-600 hover:text-pink-700 font-semibold text-xs">
                                  Show full reason
                                </summary>
                                <div className="mt-2 p-3 bg-pink-50 rounded-lg border border-pink-200 text-black text-sm whitespace-pre-wrap">
                                  {finding.reason}
                                </div>
                              </details>
                            </div>
                          ) : (
                            <span className="text-black text-sm">{finding.reason}</span>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {!loading && !error && results && findings.length === 0 && (
            <div className="text-center py-16">
              <div className="w-24 h-24 mx-auto mb-6 bg-gradient-to-br from-pink-100 to-rose-100 rounded-full flex items-center justify-center shadow-lg animate-pulse">
              </div>
              <p className="text-xl font-semibold text-black mb-2">No findings available</p>
              <p className="text-sm text-black/70">No bugs or issues were found in the analysis</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ResultsModal;

