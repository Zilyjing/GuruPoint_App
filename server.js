const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

const PORT = 3000;
const DB_FILE = path.join(__dirname, 'database.json');
const ADB_PATH = 'C:\\Users\\Dell\\AppData\\Local\\Android\\Sdk\\platform-tools\\adb.exe';

// Initial database matching https://satta-results.com/
let db = {
    users: [
        { id: 'usr_9999999999', mobile: '9999999999', name: 'GuruPoint Admin', balance: 1000000, agentCode: 'MASTER_ADMIN', role: 'ADMIN', status: 'ACTIVE', adminOverride: true },
        { id: 'usr_9027750140', mobile: '9027750140', name: 'vignes', balance: 500, agentCode: 'AG1003', role: 'MEMBER', status: 'ACTIVE', adminOverride: true }
    ],
    agents: [
        { id: 'ag1', code: 'AG1001', name: 'Royal Agent', mobile: '8888888888', pass: '1234', balance: 50000, status: 'ACTIVE' },
        { id: 'ag2', code: 'AG1002', name: 'Golden Star Agent', mobile: '7777777777', pass: '1234', balance: 25000, status: 'ACTIVE' },
        { id: 'ag3', code: 'AG1003', name: 'vinod', mobile: '9027750140', pass: '1234', balance: 10000, status: 'ACTIVE' }
    ],
    games: [
        { id: 'g1', name: 'Delhi Bazar', description: 'Delhi Bazar Live Draw', openTime: '10:00 AM', closeTime: '02:30 PM', resultTime: '03:00 PM', latestResult: '91', isOpen: true, sourceUrl: 'https://satta-results.com/' },
        { id: 'g2', name: 'Shree Ganesh', description: 'Shree Ganesh Daily Draw', openTime: '11:00 AM', closeTime: '04:15 PM', resultTime: '04:30 PM', latestResult: '47', isOpen: true, sourceUrl: 'https://satta-results.com/' },
        { id: 'g3', name: 'Faridabad', description: 'Faridabad Evening Draw', openTime: '12:00 PM', closeTime: '05:45 PM', resultTime: '06:15 PM', latestResult: '36', isOpen: true, sourceUrl: 'https://satta-results.com/' },
        { id: 'g4', name: 'Ghaziabad', description: 'Ghaziabad Night Draw', openTime: '01:00 PM', closeTime: '08:15 PM', resultTime: '08:30 PM', latestResult: '98', isOpen: true, sourceUrl: 'https://satta-results.com/' },
        { id: 'g5', name: 'Gali', description: 'Gali Midnight Draw', openTime: '02:00 PM', closeTime: '11:00 PM', resultTime: '11:30 PM', latestResult: '80', isOpen: true, sourceUrl: 'https://satta-results.com/' },
        { id: 'g6', name: 'Disawar', description: 'Disawar Early Morning Draw', openTime: '03:00 PM', closeTime: '03:00 AM', resultTime: '05:00 AM', latestResult: '15', isOpen: false, sourceUrl: 'https://satta-results.com/' }
    ],
    results: [
        { id: 'res_1', gameId: 'g1', gameName: 'Delhi Bazar', resultDate: '2025-02-18', resultTime: '03:00 PM', declaredNumber: '91', isDeclared: true, sourceName: 'https://satta-results.com/' },
        { id: 'res_2', gameId: 'g2', gameName: 'Shree Ganesh', resultDate: '2025-02-18', resultTime: '04:30 PM', declaredNumber: '47', isDeclared: true, sourceName: 'https://satta-results.com/' },
        { id: 'res_3', gameId: 'g3', gameName: 'Faridabad', resultDate: '2025-02-18', resultTime: '06:15 PM', declaredNumber: '36', isDeclared: true, sourceName: 'https://satta-results.com/' },
        { id: 'res_4', gameId: 'g4', gameName: 'Ghaziabad', resultDate: '2025-02-18', resultTime: '08:30 PM', declaredNumber: '98', isDeclared: true, sourceName: 'https://satta-results.com/' },
        { id: 'res_5', gameId: 'g5', gameName: 'Gali', resultDate: '2025-02-18', resultTime: '11:30 PM', declaredNumber: '80', isDeclared: true, sourceName: 'https://satta-results.com/' },
        { id: 'res_6', gameId: 'g6', gameName: 'Disawar', resultDate: '2025-02-18', resultTime: '05:00 AM', declaredNumber: '15', isDeclared: true, sourceName: 'https://satta-results.com/' }
    ],
    logs: [
        { action: 'SERVER_START', details: 'Satta-Results.com Chrome-Header Scraper Active', time: new Date().toLocaleTimeString() }
    ]
};

// Load database if exists
if (fs.existsSync(DB_FILE)) {
    try {
        const fileData = fs.readFileSync(DB_FILE, 'utf8');
        db = JSON.parse(fileData);
    } catch (e) {
        console.error("Error reading database file", e);
    }
}

function pushToADB() {
    if (!fs.existsSync(ADB_PATH)) return;
    const cmd = `"${ADB_PATH}" push "${DB_FILE}" /sdcard/Download/gurupoint_users_db.json`;
    exec(cmd, (err) => {
        if (!err) {
            console.log("🟢 ADB PUSH SUCCESS: Pushed database.json directly to Android Emulator / Device!");
        }
    });
}

function saveDatabase() {
    try {
        fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2), 'utf8');
        pushToADB();
    } catch (e) {
        console.error("Error saving database file", e);
    }
}

// --- REAL CHROME-HEADER LIVE SCRAPER FROM https://satta-results.com/ ---
function fetchLiveResultsFromSattaResultsCom() {
    const options = {
        hostname: 'satta-results.com',
        port: 443,
        path: '/',
        method: 'GET',
        headers: {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.9'
        }
    };

    const req = https.request(options, (res) => {
        let data = '';
        res.on('data', chunk => { data += chunk; });
        res.on('end', () => {
            try {
                const matches = data.match(/<td[^>]*>(.*?)<\/td>/gis);
                if (matches) {
                    const cells = matches.map(m => m.replace(/<[^>]*>/g, '').trim());
                    const mapping = {
                        'Delhi Bazar': ['DELHI BAZAR', 'DLB'],
                        'Shree Ganesh': ['SHREE GANESH', 'SRG'],
                        'Faridabad': ['FARIDABAD', 'FBD'],
                        'Ghaziabad': ['GHAZIABAD', 'GZB'],
                        'Gali': ['GALI'],
                        'Disawar': ['DESAWER', 'DSR', 'DISAWAR']
                    };

                    let updated = false;
                    db.games.forEach(game => {
                        const keys = mapping[game.name] || [game.name.toUpperCase()];
                        for (let i = 0; i < cells.length; i++) {
                            const cellText = cells[i].toUpperCase();
                            if (keys.some(k => cellText.includes(k))) {
                                for (let j = 1; j <= 3; j++) {
                                    if (cells[i + j] && /^\d{1,2}$/.test(cells[i + j])) {
                                        const liveNum = cells[i + j].padStart(2, '0');
                                        if (game.latestResult !== liveNum && liveNum !== '00') {
                                            game.latestResult = liveNum;
                                            const todayStr = new Date().toISOString().split('T')[0];

                                            if (!db.results.some(r => r.gameId === game.id && r.declaredNumber === liveNum && r.resultDate === todayStr)) {
                                                db.results.unshift({
                                                    id: 'res_' + Date.now() + '_' + game.id,
                                                    gameId: game.id,
                                                    gameName: game.name,
                                                    resultDate: todayStr,
                                                    resultTime: game.resultTime,
                                                    declaredNumber: liveNum,
                                                    isDeclared: true,
                                                    sourceName: 'https://satta-results.com/ (Live Scraped)'
                                                });
                                            }
                                            updated = true;
                                            console.log(`🎯 SATTA-RESULTS.COM SCRAPED LIVE SUCCESS: ${game.name} -> ${liveNum}`);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    });

                    if (updated) {
                        saveDatabase();
                    }
                }
            } catch (e) {
                console.error("Error scraping https://satta-results.com/:", e.message);
            }
        });
    });

    req.on('error', (err) => {
        console.error("Failed to connect to https://satta-results.com/:", err.message);
    });

    req.end();
}

// Fetch live results from https://satta-results.com/ every 30 seconds
setInterval(fetchLiveResultsFromSattaResultsCom, 30000);
setTimeout(fetchLiveResultsFromSattaResultsCom, 2000);

// --- AUTOMATIC ADB DIRECT DISK WATCHER ---
function syncViaADB() {
    if (!fs.existsSync(ADB_PATH)) return;
    const cmd1 = `"${ADB_PATH}" shell "cat /sdcard/Download/gurupoint_users_db.json"`;
    exec(cmd1, (err, stdout) => {
        if (!err && stdout && stdout.includes('{')) {
            try {
                const parsedMap = JSON.parse(stdout);
                mergeUsersFromObjectMap(parsedMap, false);
            } catch (e) {}
        }
    });
}

function mergeUsersFromObjectMap(parsedMap, isFromAdmin = false) {
    let userList = [];
    if (Array.isArray(parsedMap)) {
        userList = parsedMap;
    } else if (typeof parsedMap === 'object') {
        userList = Object.values(parsedMap);
    }

    let updated = false;
    userList.forEach(u => {
        if (u && (u.mobileNumber || u.mobile)) {
            const mobile = (u.mobileNumber || u.mobile).toString().trim();
            const rawName = u.name || u.fullName || u.userName;
            const agentCode = u.assignedAgentCode || u.agentCode || 'UNASSIGNED';
            const balance = u.coinBalance !== undefined ? u.coinBalance : (u.balance !== undefined ? u.balance : 500);
            const status = u.status || 'ACTIVE';

            const idx = db.users.findIndex(existing => existing.mobile.toString().trim() === mobile);
            if (idx !== -1) {
                const currentName = db.users[idx].name;
                const nameToUse = (rawName && !rawName.startsWith('Gamer ')) ? rawName : (currentName || `Gamer ${mobile}`);

                if (isFromAdmin) {
                    db.users[idx].balance = balance;
                    db.users[idx].status = status;
                    if (agentCode) db.users[idx].agentCode = agentCode;
                    db.users[idx].name = nameToUse;
                    db.users[idx].adminOverride = true;
                    updated = true;
                } else if (!db.users[idx].adminOverride) {
                    if (db.users[idx].balance !== balance || db.users[idx].agentCode !== agentCode || (rawName && db.users[idx].name !== rawName)) {
                        db.users[idx].balance = balance;
                        db.users[idx].agentCode = agentCode;
                        if (rawName && !rawName.startsWith('Gamer ')) db.users[idx].name = rawName;
                        updated = true;
                    }
                }
            } else {
                const nameToUse = (rawName && rawName.trim() !== '') ? rawName : `Gamer ${mobile}`;
                db.users.push({
                    id: `usr_${mobile}`,
                    mobile: mobile,
                    name: nameToUse,
                    balance: balance,
                    agentCode: agentCode,
                    role: u.role || 'MEMBER',
                    status: status,
                    adminOverride: isFromAdmin
                });
                updated = true;
            }
        }
    });

    if (updated) {
        saveDatabase();
    }
}

setInterval(syncViaADB, 2000);

const server = http.createServer((req, res) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, DELETE');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

    if (req.method === 'OPTIONS') {
        res.writeHead(200);
        res.end();
        return;
    }

    const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);
    const pathname = parsedUrl.pathname;

    // --- REST API ENDPOINTS ---
    if (pathname === '/api/users' && req.method === 'GET') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(db.users));
        return;
    }

    if (pathname === '/api/agents' && req.method === 'GET') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(db.agents));
        return;
    }

    if (pathname === '/api/games' && req.method === 'GET') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(db.games));
        return;
    }

    if (pathname === '/api/results' && req.method === 'GET') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(db.results));
        return;
    }

    if (pathname === '/api/save_game' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => { body += chunk; });
        req.on('end', () => {
            try {
                const gameData = JSON.parse(body);
                const existingIdx = db.games.findIndex(g => g.id === gameData.id);
                if (existingIdx !== -1) {
                    db.games[existingIdx] = gameData;
                } else {
                    db.games.push(gameData);
                }
                saveDatabase();
                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: true, games: db.games }));
            } catch (err) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, error: err.message }));
            }
        });
        return;
    }

    if (pathname === '/api/declare_result' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => { body += chunk; });
        req.on('end', () => {
            try {
                const resData = JSON.parse(body);
                const game = db.games.find(g => g.id === resData.gameId);
                if (game) {
                    game.latestResult = resData.declaredNumber;
                }

                db.results.unshift({
                    id: 'res_' + Date.now(),
                    gameId: resData.gameId,
                    gameName: resData.gameName,
                    resultDate: new Date().toISOString().split('T')[0],
                    resultTime: game ? game.resultTime : '03:00 PM',
                    declaredNumber: resData.declaredNumber,
                    isDeclared: true,
                    sourceName: resData.sourceName || 'https://satta-results.com/'
                });

                db.logs.unshift({
                    action: 'MANUAL_DECLARE_RESULT',
                    details: `Master Admin declared result [${resData.declaredNumber}] for ${resData.gameName} from https://satta-results.com/`,
                    time: new Date().toLocaleTimeString()
                });

                saveDatabase();

                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: true, message: 'Result declared successfully!' }));
            } catch (err) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, error: err.message }));
            }
        });
        return;
    }

    if (pathname === '/api/register_agent' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => { body += chunk; });
        req.on('end', () => {
            try {
                const newAgent = JSON.parse(body);
                const mobile = newAgent.mobile.toString().trim();
                if (db.agents.some(a => a.mobile === mobile)) {
                    res.writeHead(400, { 'Content-Type': 'application/json' });
                    res.end(JSON.stringify({ success: false, error: 'Agent already registered!' }));
                    return;
                }

                const code = 'AG' + (1001 + db.agents.length);
                const agentObj = {
                    id: 'ag_' + Date.now(),
                    code: code,
                    name: newAgent.name,
                    mobile: mobile,
                    pass: newAgent.pass,
                    balance: 10000,
                    status: 'ACTIVE'
                };
                db.agents.push(agentObj);
                saveDatabase();

                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: true, agent: agentObj }));
            } catch (err) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, error: err.message }));
            }
        });
        return;
    }

    if (pathname === '/api/update_user' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => { body += chunk; });
        req.on('end', () => {
            try {
                const updateData = JSON.parse(body);
                const mobile = (updateData.mobileNumber || updateData.mobile).toString().trim();
                let idx = db.users.findIndex(u => u.mobile.toString().trim() === mobile);

                if (idx === -1) {
                    const newUser = {
                        id: `usr_${mobile}`,
                        mobile: mobile,
                        name: updateData.name || `Gamer ${mobile}`,
                        balance: updateData.balance !== undefined ? updateData.balance : 500,
                        agentCode: updateData.agentCode || 'UNASSIGNED',
                        role: 'MEMBER',
                        status: updateData.status || 'ACTIVE',
                        adminOverride: true
                    };
                    db.users.push(newUser);
                    idx = db.users.length - 1;
                } else {
                    if (updateData.balance !== undefined) db.users[idx].balance = updateData.balance;
                    if (updateData.status) db.users[idx].status = updateData.status;
                    if (updateData.agentCode) db.users[idx].agentCode = updateData.agentCode;
                    if (updateData.name && !updateData.name.startsWith('Gamer ')) db.users[idx].name = updateData.name;
                    db.users[idx].adminOverride = true;
                }

                saveDatabase();

                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: true, user: db.users[idx] }));
            } catch (err) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, error: err.message }));
            }
        });
        return;
    }

    if (pathname === '/api/register' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => { body += chunk; });
        req.on('end', () => {
            try {
                const newUser = JSON.parse(body);
                mergeUsersFromObjectMap([newUser], false);
                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: true, message: 'User registered & synced!' }));
            } catch (err) {
                res.writeHead(400, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ success: false, error: err.message }));
            }
        });
        return;
    }

    if (pathname === '/api/logs' && req.method === 'GET') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(db.logs));
        return;
    }

    // --- WEB PORTAL PAGES SERVING ---
    if (pathname === '/admin' || pathname === '/admin/') {
        const filePath = path.join(__dirname, 'admin_portal', 'index.html');
        fs.readFile(filePath, (err, data) => {
            if (err) { res.writeHead(500); res.end('Error loading Admin Portal'); }
            else { res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' }); res.end(data); }
        });
        return;
    }

    if (pathname === '/agent' || pathname === '/agent/') {
        const filePath = path.join(__dirname, 'agent_portal', 'index.html');
        fs.readFile(filePath, (err, data) => {
            if (err) { res.writeHead(500); res.end('Error loading Agent Portal'); }
            else { res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' }); res.end(data); }
        });
        return;
    }

    res.writeHead(404);
    res.end('Endpoint not found');
});

server.listen(PORT, () => {
    console.log(`====================================================`);
    console.log(`🚀 GuruPoint Satta-Results.com Chrome-Header Scraper Running!`);
    console.log(`🌐 Master Admin Portal: http://localhost:${PORT}/admin`);
    console.log(`🌐 Agent Portal:        http://localhost:${PORT}/agent`);
    console.log(`====================================================`);
});
