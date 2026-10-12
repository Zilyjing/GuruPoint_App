const https = require('https');

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
        console.log("HTTP STATUS:", res.statusCode);
        const matches = data.match(/<td[^>]*>(.*?)<\/td>/gis);
        if (matches) {
            const cells = matches.map(m => m.replace(/<[^>]*>/g, '').trim());
            console.log("EXTRACTED LIVE CELLS SAMPLE:", cells.slice(0, 20));
        }
    });
});

req.on('error', (e) => {
    console.error("HTTP ERROR:", e.message);
});

req.end();
