const express = require('express')
const app = express()

app.get('/light/on', (req, res) => {
  console.log('encendido')
  res.send('encendido')
})

app.get('/light/off', (req, res) => {
  console.log('apagado')
  res.send('apagado')
})

app.listen(3000, () => console.log('Listening on port 3000!'))
