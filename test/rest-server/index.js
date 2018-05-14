const express = require('express')
const app = express()

app.get('/light/:number/on', (req, res) => {
  number = req.params.number
  message = `Powering on light ${number}`
  console.log(message)
  res.send(message)
})

app.get('/light/:number/off', (req, res) => {
  number = req.params.number
  message = `Powering off light ${number}`
  console.log(message)
  res.send(message)
})

app.listen(3000, () => console.log('Listening on port 3000!'))
