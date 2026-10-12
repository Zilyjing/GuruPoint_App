const https = require('https');

https.get('https://satta-results.com/', (res) => {
    let data = '';
    res.on('data', chunk => { data += chunk; });
    res.on('end', () => {
        // Find all <td> or result numbers
        const matches = data.match(/<td[^>]*>(.*?)<\/td>/gis);
        if (matches) {
            console.log("Found table cells:", matches.slice(0, 30).map(m => m.replace(/<[^>]*>/g, '').trim()));
        } else {
            console.log("No table cells found");
        }
    });
}).on('error', (err) => {
    console.error("ERROR:", err.message);
});
