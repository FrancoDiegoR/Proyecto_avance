// Backend simulado de SmartGas.Api para probar la app Android sin el backend real.
// Uso: node docs/mock/server.js   (escucha en http://localhost:5048/api/v1)
// Los datos salen de docs/mock/db.json y los cambios solo viven en memoria.
const http = require('http');
const fs = require('fs');
const path = require('path');
const db = JSON.parse(fs.readFileSync(process.argv[2] || path.join(__dirname, 'db.json'), 'utf8'));
const now = () => new Date().toISOString();

db.incidents[0].status = 'Active';
db.incidents.push({ id: 2, accountId: 1, type: 'HighTemperature', zoneId: 1, zoneName: 'Main Kitchen', sensorId: 2, sensorCode: 'SG-002',
  detectedValue: 'Temp: 61 °C', temperature: 61, severity: 'Critical', status: 'Resolved', detectedAt: '2026-05-10T10:00:00Z' });
db.alerts[0].status = 'Active';
db.sensors.forEach(s => { s.type = s.type === 'Gas LP' ? 'Gas' : s.type; });

const send = (res, code, body) => { res.writeHead(code, { 'Content-Type': 'application/json' }); res.end(JSON.stringify(body)); };
const byAccount = list => list.filter(x => x.accountId === 1);

http.createServer((req, res) => {
  let raw = '';
  req.on('data', c => raw += c);
  req.on('end', () => {
    const body = raw ? JSON.parse(raw) : {};
    const url = new URL(req.url, 'http://x');
    const p = url.pathname.replace('/api/v1', '');
    const m = req.method;
    console.log(m, p, raw);
    let r;

    if (m === 'POST' && p === '/auth/sign-in') {
      const a = db.accounts.find(x => x.email === body.email && x.password === body.password);
      return a ? send(res, 200, { accountId: a.id, email: a.email, fullName: a.name, role: 'RestaurantAdministrator', status: 'Active', token: 't' }) : send(res, 401, { message: 'Invalid credentials' });
    }
    if (m === 'POST' && p === '/auth/sign-up') return send(res, 409, { message: 'Email already registered' });
    if (m === 'GET' && p === '/plans') return send(res, 200, db.plans);
    if ((r = p.match(/^\/subscriptions\/current\/(\d+)$/)) && m === 'GET') {
      const s = db.subscriptions[0]; const pl = db.plans.find(x => x.id === s.planId);
      return send(res, 200, { ...s, planName: pl.name, price: pl.price, maxZones: pl.maxZones, maxSensors: pl.maxSensors });
    }
    if ((r = p.match(/^\/subscriptions\/current\/(\d+)\/change-plan$/)) && m === 'PATCH') {
      db.subscriptions[0].planId = body.planId; const pl = db.plans.find(x => x.id === body.planId);
      return send(res, 200, { ...db.subscriptions[0], planName: pl.name, price: pl.price, maxZones: pl.maxZones, maxSensors: pl.maxSensors });
    }
    if (m === 'GET' && p === '/zones') return send(res, 200, byAccount(db.zones));
    if (m === 'POST' && p === '/zones') {
      if (db.zones.length >= 3 && db.subscriptions[0].planId === 1) return send(res, 409, { message: 'Zone limit reached for plan' });
      const z = { id: db.zones.length + 1, status: 'Safe', createdAt: now(), ...body }; db.zones.push(z); return send(res, 201, z);
    }
    if (m === 'GET' && p === '/sensors') return send(res, 200, byAccount(db.sensors));
    if (m === 'POST' && p === '/sensors') { const s = { id: db.sensors.length + 1, status: 'Online', batteryLevel: 100, createdAt: now(), ...body }; db.sensors.push(s); return send(res, 201, s); }
    if ((r = p.match(/^\/sensors\/(\d+)$/)) && m === 'PATCH') { const s = db.sensors.find(x => x.id === +r[1]); Object.assign(s, body); return send(res, 200, s); }
    if (m === 'GET' && p === '/sensor-readings') return send(res, 200, byAccount(db.sensorReadings));
    if (m === 'POST' && p === '/sensor-readings') {
      const s = db.sensors.find(x => x.code === body.sensorCode);
      db.sensorReadings.push({ id: db.sensorReadings.length + 1, accountId: 1, sensorId: s.id, zoneId: s.zoneId, gasLevel: body.gasLevel, temperature: body.temperature, createdAt: now() });
      const created = body.gasLevel >= 50 || body.temperature >= 45;
      if (created) {
        const z = db.zones.find(x => x.id === s.zoneId); z.status = 'Critical';
        const id = db.incidents.length + 1;
        db.incidents.push({ id, accountId: 1, type: 'GasLeak', zoneId: z.id, zoneName: z.name, sensorId: s.id, sensorCode: s.code, gasLevel: body.gasLevel, detectedValue: `Gas: ${body.gasLevel} ppm`, severity: 'Critical', status: 'Active', detectedAt: now() });
        db.notifications.push({ id: db.notifications.length + 1, accountId: 1, incidentId: id, message: `Sensor ${s.code} detected a GasLeak event in zone ${z.name}.`, channel: 'Web', isRead: false, isConfirmed: false, createdAt: now() });
      }
      return send(res, 201, { incidentCreated: created, severity: created ? 'Critical' : null, incidentType: created ? 'GasLeak' : null });
    }
    if (m === 'GET' && p === '/incidents') return send(res, 200, byAccount(db.incidents));
    if ((r = p.match(/^\/incidents\/(\d+)\/(review|resolve|false-alarm)$/)) && m === 'PATCH') {
      const i = db.incidents.find(x => x.id === +r[1]); i.status = { review: 'Reviewed', resolve: 'Resolved', 'false-alarm': 'FalseAlarm' }[r[2]]; return send(res, 200, i);
    }
    if (m === 'GET' && p === '/alerts') return send(res, 200, byAccount(db.alerts));
    if (m === 'GET' && p === '/notifications') return send(res, 200, byAccount(db.notifications));
    if ((r = p.match(/^\/notifications\/(\d+)\/(read|confirm)$/)) && m === 'PATCH') {
      const n = db.notifications.find(x => x.id === +r[1]); if (r[2] === 'read') { n.read = true; n.isRead = true; } else { n.confirmed = true; n.isConfirmed = true; } return send(res, 200, n);
    }
    if ((r = p.match(/^\/profiles\/(\d+)$/))) {
      const pr = db.profiles[0]; if (m === 'PATCH') Object.assign(pr, body);
      return send(res, 200, { ...pr, createdAt: '2026-04-15T12:00:00Z', updatedAt: now() });
    }
    if ((r = p.match(/^\/settings\/(\d+)$/))) {
      db._s = db._s || { language: 'en-US', darkMode: false, notificationsEnabled: true, gasThreshold: 50, temperatureThreshold: 45 };
      if (m === 'PATCH') Object.assign(db._s, body); return send(res, 200, db._s);
    }
    if ((r = p.match(/^\/emergency-contacts\/(\d+)$/))) {
      db._e = db._e || { id: 1, accountId: 1, name: 'Rosa Vargas', phone: '+51 998 877 665', email: 'emergencias@smartgas.com' };
      if (m === 'PATCH') Object.assign(db._e, body); return send(res, 200, db._e);
    }
    if (m === 'GET' && p === '/external/weather/current') return send(res, 200, { temperature: 19.4, temperatureUnit: '°C', relativeHumidity: 82, relativeHumidityUnit: '%', windSpeed: 11.2, windSpeedUnit: 'km/h', source: 'Open-Meteo' });
    send(res, 404, { message: 'not found' });
  });
}).listen(5048, () => console.log('SmartGas mock API en http://localhost:5048/api/v1'));
