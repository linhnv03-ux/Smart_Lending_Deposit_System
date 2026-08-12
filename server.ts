import express from 'express';

const app = express();
app.use(express.json());

// In-Memory state for both Microservices in Node preview mode
let circuitBreakerState: 'CLOSED' | 'OPEN' = 'CLOSED';
let simulatedErrorMode = false;

const auditLogs: any[] = [
  {
    auditId: 'ES-AUDIT-INIT-001',
    service: 'slds-loan-service',
    action: 'SERVICE_INITIALIZATION',
    userId: 'SYS_ADMIN',
    contractNo: 'N/A',
    details: 'SLDS Gateway & Auth Service (Port 8080) and Loan Microservice (Port 8081) started on Java 17 / Spring Boot 3.2.3',
    status: 'SUCCESS',
    executionTimeMs: 14,
    timestamp: new Date().toISOString()
  }
];

const loanApplications = new Map<string, any>();
const activeSessions = new Map<string, any>();

// Root Overview
app.get('/', (req, res) => {
  res.json({
    project: 'Smart Lending & Deposit System (SLDS)',
    architecture: 'Microservices Architecture (2 Services)',
    services: [
      {
        name: 'slds-gateway-auth-service',
        port: 8080,
        responsibilities: [
          'Client Single Entrypoint',
          'Authentication & Authorization (JWT)',
          'Redis Session Management',
          'Redis Rate Limiting (DDoS Protection)',
          'Spring Cloud Gateway Route Forwarding'
        ],
        techStack: ['Spring Cloud Gateway', 'Spring Security WebFlux', 'Reactive Redis', 'JJWT']
      },
      {
        name: 'slds-loan-service',
        port: 8081,
        responsibilities: [
          'Loan Applications Management',
          'Interest Rate Calculation (Strategy Pattern: Flat Rate vs Reducing Balance)',
          'Repayment Schedule Generation',
          'Disbursement & Repayment Collection',
          'ActiveMQ Event Publishing for Credit Assessment',
          'Resilience4j Circuit Breaker for Core Banking',
          'Elasticsearch Audit Logging'
        ],
        techStack: ['Spring Boot 3', 'PostgreSQL', 'Redis', 'ActiveMQ', 'Resilience4j', 'Elasticsearch']
      }
    ],
    postmanCollection: '/SLDS_Postman_Collection.json',
    apiEndpoints: {
      auth: {
        login: 'POST /api/v1/auth/login',
        validateToken: 'GET /api/v1/auth/validate'
      },
      loanApplications: {
        createApplication: 'POST /api/v1/loans/applications',
        getApplication: 'GET /api/v1/loans/applications/:appNo'
      },
      disbursementAndRepayment: {
        disburseLoan: 'POST /api/v1/loans/disburse',
        repayLoan: 'POST /api/v1/loans/repay',
        previewSchedule: 'GET /api/v1/loans/schedules/preview'
      },
      resilienceAndAudit: {
        circuitBreakerStatus: 'GET /api/v1/loans/circuit-breaker/status',
        toggleCircuitBreaker: 'POST /api/v1/loans/circuit-breaker/toggle',
        auditLogs: 'GET /api/v1/loans/audit-logs'
      }
    }
  });
});

// --- SERVICE 1: API GATEWAY & AUTH SERVICE ENDPOINTS (/api/v1/auth/*) ---

app.post('/api/v1/auth/login', (req, res) => {
  const { username, password, role } = req.body;
  if (!username) {
    return res.status(400).json({ error: 'Username is required' });
  }

  const token = `eyJhbGciOiJIUzI1NiJ9.slds_jwt_session_${username}_${Date.now()}`;
  const userRole = role || 'CREDIT_OFFICER';
  const userId = `USR-${Math.abs(username.split('').reduce((a: number, b: string) => a + b.charCodeAt(0), 0))}`;

  const session = {
    token,
    username,
    role: userRole,
    userId,
    branchCode: 'BRANCH_HO',
    issuedAt: new Date().toISOString()
  };

  activeSessions.set(token, session);

  return res.json({
    success: true,
    token,
    tokenType: 'Bearer',
    expiresInMs: 86400000,
    userId,
    username,
    role: userRole,
    branchCode: 'BRANCH_HO',
    issuedAt: session.issuedAt,
    gatewayRoute: 'slds-gateway-auth-service -> Session cached in Redis'
  });
});

app.get('/api/v1/auth/validate', (req, res) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.json({ valid: false, message: 'Missing Authorization Header' });
  }
  const token = authHeader.substring(7);
  const session = activeSessions.get(token);

  if (session) {
    return res.json({
      valid: true,
      username: session.username,
      role: session.role,
      userId: session.userId,
      message: 'Token valid and active in Redis session store'
    });
  } else {
    return res.json({ valid: false, message: 'Invalid or expired session' });
  }
});

// --- SERVICE 2: LOAN MICROSERVICE ENDPOINTS (/api/v1/loans/*) ---

// 1. Create Loan Application (Khởi tạo hồ sơ & Phát ActiveMQ Event)
app.post('/api/v1/loans/applications', (req, res) => {
  const { applicantId, applicantName, applicantCip, requestedAmount, termMonths, interestRate, interestType, purpose, officerId } = req.body;

  if (!applicantId || !requestedAmount || !termMonths || !interestRate) {
    return res.status(400).json({ error: 'Validation Failed', message: 'Missing required loan application fields' });
  }

  const appNo = `HS-VAY-${Math.floor(Math.random() * 90000000 + 10000000)}`;
  const activeMqMessageId = `AMQ-MSG-${Math.random().toString(36).substring(2, 10).toUpperCase()}`;

  const newApp = {
    id: Date.now(),
    applicationNo: appNo,
    applicantId,
    applicantName,
    applicantCip,
    requestedAmount: Number(requestedAmount),
    termMonths: Number(termMonths),
    interestRate: Number(interestRate),
    interestType: interestType || 'REDUCING_BALANCE',
    purpose: purpose || 'Vay tiêu dùng',
    status: 'ASSESSING',
    creditScore: 735,
    officerId: officerId || 'OFFICER_01',
    branchCode: 'BRANCH_HO',
    activeMqMessageId,
    createdAt: new Date().toISOString()
  };

  loanApplications.set(appNo, newApp);

  // Audit Log
  auditLogs.unshift({
    auditId: `ES-AUDIT-${Math.random().toString(36).substring(2, 10).toUpperCase()}`,
    service: 'slds-loan-service',
    action: 'CREATE_LOAN_APPLICATION',
    userId: officerId || 'OFFICER_01',
    contractNo: appNo,
    details: `Created loan application for ${applicantName} (${requestedAmount.toLocaleString('vi-VN')} VND). Published to ActiveMQ queue 'loan.application.assessment'.`,
    status: 'SUCCESS',
    executionTimeMs: 18,
    timestamp: new Date().toISOString()
  });

  return res.json({
    ...newApp,
    status: 'APPROVED',
    message: 'Hồ sơ vay đã khởi tạo thành công và phát Event tới ActiveMQ [loan.application.assessment] để chấm điểm tín dụng'
  });
});

app.get('/api/v1/loans/applications/:appNo', (req, res) => {
  const app = loanApplications.get(req.params.appNo);
  if (!app) {
    return res.status(404).json({ error: 'Loan application not found' });
  }
  return res.json(app);
});

// 2. Loan Disbursement
app.post('/api/v1/loans/disburse', (req, res) => {
  const startTime = Date.now();
  const { applicationNo, disbursementAccount, loanAmount, termMonths, interestRate, interestType, officerId } = req.body;

  if (!disbursementAccount || !loanAmount || !termMonths || !interestRate) {
    return res.status(400).json({ error: 'Validation Failed', message: 'Required disbursement fields missing' });
  }

  const contractNo = `HDTD-${Math.floor(Math.random() * 90000000 + 10000000)}`;
  const disbursementId = `DISB-${Math.random().toString(36).substring(2, 10).toUpperCase()}`;

  const amount = Number(loanAmount);
  const term = Number(termMonths);
  const rate = Number(interestRate);

  const schedule: any[] = [];
  let remainingBalance = amount;
  let totalInterest = 0;
  const monthlyPrincipal = +(amount / term).toFixed(2);

  if (interestType === 'FLAT_RATE') {
    const monthlyInterest = +((amount * (rate / 100)) / 12).toFixed(2);
    const monthlyInstallment = +(monthlyPrincipal + monthlyInterest).toFixed(2);
    totalInterest = +(monthlyInterest * term).toFixed(2);

    for (let p = 1; p <= term; p++) {
      remainingBalance = p === term ? 0 : +(remainingBalance - monthlyPrincipal).toFixed(2);
      const dueDate = new Date();
      dueDate.setMonth(dueDate.getMonth() + p);
      schedule.push({
        period: p,
        dueDate: dueDate.toISOString().split('T')[0],
        principalPayable: monthlyPrincipal,
        interestPayable: monthlyInterest,
        totalInstallment: monthlyInstallment,
        remainingBalance: Math.max(0, remainingBalance)
      });
    }
  } else {
    // REDUCING_BALANCE
    const monthlyRate = rate / 100 / 12;
    for (let p = 1; p <= term; p++) {
      const monthlyInterest = +(remainingBalance * monthlyRate).toFixed(2);
      totalInterest += monthlyInterest;
      remainingBalance = p === term ? 0 : +(remainingBalance - monthlyPrincipal).toFixed(2);
      const dueDate = new Date();
      dueDate.setMonth(dueDate.getMonth() + p);
      schedule.push({
        period: p,
        dueDate: dueDate.toISOString().split('T')[0],
        principalPayable: monthlyPrincipal,
        interestPayable: monthlyInterest,
        totalInstallment: +(monthlyPrincipal + monthlyInterest).toFixed(2),
        remainingBalance: Math.max(0, remainingBalance)
      });
    }
    totalInterest = +totalInterest.toFixed(2);
  }

  const fallbackTriggered = simulatedErrorMode;
  const executionTimeMs = Date.now() - startTime + 16;

  const coreJournalRef = fallbackTriggered
    ? `FALLBACK-QUEUE-${Math.random().toString(36).substring(2, 8).toUpperCase()}`
    : `CB-JRN-${Math.random().toString(36).substring(2, 8).toUpperCase()}`;

  const auditId = `ES-AUDIT-${Math.random().toString(36).substring(2, 10).toUpperCase()}`;

  auditLogs.unshift({
    auditId,
    service: 'slds-loan-service',
    action: 'DISBURSE_LOAN',
    userId: officerId || 'OFFICER_01',
    contractNo,
    details: `Disbursed ${amount.toLocaleString('vi-VN')} VND to account ${disbursementAccount} via Strategy [${interestType || 'REDUCING_BALANCE'}]`,
    status: fallbackTriggered ? 'WARNING' : 'SUCCESS',
    executionTimeMs,
    timestamp: new Date().toISOString()
  });

  return res.json({
    success: true,
    disbursementId,
    loanContractNo: contractNo,
    status: 'DISBURSED',
    applicantId: `CUST-${Math.abs(disbursementAccount.hashCode?.() || 8849201)}`,
    applicantName: `Khách hàng Vay ${contractNo}`,
    disbursedAmount: amount,
    interestRate: rate,
    interestType: interestType || 'REDUCING_BALANCE',
    termMonths: term,
    monthlyInstallment: schedule[0]?.totalInstallment || 0,
    totalInterest,
    totalRepayment: +(amount + totalInterest).toFixed(2),
    disbursementDate: new Date().toISOString(),
    disbursementAccount,
    coreBankingJournalRef: coreJournalRef,
    processingTimeMs: executionTimeMs,
    redisCacheStatus: 'HIT',
    circuitBreakerState: fallbackTriggered ? 'OPEN (Fallback Active)' : 'CLOSED (Healthy)',
    activeMqMessageId: `AMQ-MSG-${Math.random().toString(36).substring(2, 8).toUpperCase()}`,
    elasticsearchAuditId: auditId,
    repaymentSchedule: schedule,
    fallbackTriggered,
    fallbackReason: fallbackTriggered
      ? 'Resilience4j Circuit Breaker OPEN: Core Banking Oracle DB timeout. Handled via offline queue.'
      : null
  });
});

// 3. Repay Loan
app.post('/api/v1/loans/repay', (req, res) => {
  const { contractNo, repaymentAmount, sourceAccount, tellerId } = req.body;
  if (!contractNo || !repaymentAmount) {
    return res.status(400).json({ error: 'ContractNo and RepaymentAmount are required' });
  }

  const receiptNo = `REC-${Math.random().toString(36).substring(2, 10).toUpperCase()}`;
  const amountPaid = Number(repaymentAmount);

  auditLogs.unshift({
    auditId: `ES-AUDIT-${Math.random().toString(36).substring(2, 10).toUpperCase()}`,
    service: 'slds-loan-service',
    action: 'REPAY_LOAN',
    userId: tellerId || 'TELLER_01',
    contractNo,
    details: `Collected repayment of ${amountPaid.toLocaleString('vi-VN')} VND from account ${sourceAccount || 'N/A'}`,
    status: 'SUCCESS',
    executionTimeMs: 12,
    timestamp: new Date().toISOString()
  });

  return res.json({
    success: true,
    receiptNo,
    contractNo,
    paidAmount: amountPaid,
    principalPaid: +(amountPaid * 0.85).toFixed(2),
    interestPaid: +(amountPaid * 0.15).toFixed(2),
    remainingBalance: 0,
    status: 'CLOSED',
    paidAt: new Date().toISOString(),
    message: 'Thu nợ kỳ thành công, hợp đồng khoản vay đã được cập nhật dư nợ'
  });
});

// 4. Repayment Schedule Preview
app.get('/api/v1/loans/schedules/preview', (req, res) => {
  const amount = Number(req.query.amount || 500000000);
  const rate = Number(req.query.rate || 8.5);
  const termMonths = Number(req.query.termMonths || 12);
  const interestType = (req.query.interestType as string) || 'REDUCING_BALANCE';

  const schedule: any[] = [];
  let remainingBalance = amount;
  const monthlyPrincipal = +(amount / termMonths).toFixed(2);

  if (interestType === 'FLAT_RATE') {
    const monthlyInterest = +((amount * (rate / 100)) / 12).toFixed(2);
    const monthlyInstallment = +(monthlyPrincipal + monthlyInterest).toFixed(2);
    for (let p = 1; p <= termMonths; p++) {
      remainingBalance = p === termMonths ? 0 : +(remainingBalance - monthlyPrincipal).toFixed(2);
      const dueDate = new Date();
      dueDate.setMonth(dueDate.getMonth() + p);
      schedule.push({
        period: p,
        dueDate: dueDate.toISOString().split('T')[0],
        principalPayable: monthlyPrincipal,
        interestPayable: monthlyInterest,
        totalInstallment: monthlyInstallment,
        remainingBalance: Math.max(0, remainingBalance)
      });
    }
  } else {
    const monthlyRate = rate / 100 / 12;
    for (let p = 1; p <= termMonths; p++) {
      const monthlyInterest = +(remainingBalance * monthlyRate).toFixed(2);
      remainingBalance = p === termMonths ? 0 : +(remainingBalance - monthlyPrincipal).toFixed(2);
      const dueDate = new Date();
      dueDate.setMonth(dueDate.getMonth() + p);
      schedule.push({
        period: p,
        dueDate: dueDate.toISOString().split('T')[0],
        principalPayable: monthlyPrincipal,
        interestPayable: monthlyInterest,
        totalInstallment: +(monthlyPrincipal + monthlyInterest).toFixed(2),
        remainingBalance: Math.max(0, remainingBalance)
      });
    }
  }
  res.json(schedule);
});

// 5. Circuit Breaker & Audit Logs
app.get('/api/v1/loans/circuit-breaker/status', (req, res) => {
  res.json({
    service: 'CoreBankingAdapterService',
    state: simulatedErrorMode ? 'OPEN (Simulated Failure)' : 'CLOSED (Healthy)',
    simulatedCoreBankingDown: simulatedErrorMode
  });
});

app.post('/api/v1/loans/circuit-breaker/toggle', (req, res) => {
  simulatedErrorMode = req.body.simulateError ?? !simulatedErrorMode;
  circuitBreakerState = simulatedErrorMode ? 'OPEN' : 'CLOSED';
  res.json({
    message: simulatedErrorMode
      ? 'Core Banking Oracle DB timeout simulated. Circuit Breaker is OPEN (Fallback Active)'
      : 'Core Banking connection restored. Circuit Breaker is CLOSED.',
    circuitBreakerState
  });
});

app.get('/api/v1/loans/audit-logs', (req, res) => {
  const q = req.query.query as string;
  if (!q) return res.json(auditLogs);
  const filtered = auditLogs.filter(
    l => l.contractNo.toLowerCase().includes(q.toLowerCase()) ||
         l.details.toLowerCase().includes(q.toLowerCase()) ||
         l.action.toLowerCase().includes(q.toLowerCase())
  );
  res.json(filtered);
});

const PORT = 3000;
app.listen(PORT, '0.0.0.0', () => {
  console.log(`SLDS Microservices Proxy Server listening on port ${PORT}`);
});
