import axios from 'axios';

const API_BASE_URL = '/call';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const aiService = {
  codeInsights: async (repoUrl, branch = 'main', path = null) => {
    const payload = {
      args: path ? { path } : { repoUrl, branch }
    };
    return api.post('/ai/code_insights', payload);
  },

  defectPrediction: async (repoUrl, branch = 'main', path = null) => {
    const payload = {
      args: path ? { path } : { repoUrl, branch }
    };
    return api.post('/ai/defect_prediction', payload);
  },

  testGapAnalysis: async (repoUrl, branch = 'main', path = null) => {
    const payload = {
      args: path ? { path } : { repoUrl, branch }
    };
    return api.post('/ai/test_gap_analysis', payload);
  },

  refactorAdvisor: async (repoUrl, branch = 'main', path = null) => {
    const payload = {
      args: path ? { path } : { repoUrl, branch }
    };
    return api.post('/ai/refactor_advisor', payload);
  },

  memoryLeakPrediction: async (repoUrl, branch = 'main', path = null) => {
    const payload = {
      args: path ? { path } : { repoUrl, branch }
    };
    return api.post('/ai/memory_leak_prediction', payload);
  },
};

export const scanService = {
  scanRepository: async (repoUrl, branch = 'main') => {
    const payload = {
      requestId: `req-${Date.now()}`,
      args: { repoUrl, branch }
    };
    return api.post('/scan/repository', payload);
  },

  scanFiles: async (path) => {
    const payload = {
      requestId: `req-${Date.now()}`,
      args: { path }
    };
    return api.post('/scan/files', payload);
  },
};

export const testService = {
  runTests: async (path) => {
    const payload = {
      requestId: `req-${Date.now()}`,
      args: { path }
    };
    return api.post('/test/run', payload);
  },
};

export const reportService = {
  techDebt: async (repoUrl, branch = 'main', path = null) => {
    const payload = {
      requestId: `req-${Date.now()}`,
      args: path ? { path } : { repoUrl, branch }
    };
    return api.post('/report/tech-debt', payload);
  },
};

