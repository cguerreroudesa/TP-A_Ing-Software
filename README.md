# TP-A_Ing-Software

## Enunciado

Una empresa de retail y servicios quiere lanzar una plataforma digital de Gift Cards. El objetivo es que sus clientes puedan loggearse y obtener un token para luego reclamar y consultar el saldo o los detalles de gastos de las gift cards.

Cuando el usuario hace gastos en los comercios registrados, estos se comunican con el sistema usando su clave de merchant y generan cargos en las tarjetas que afectan su balance y detalle de cargos.

El sistema tendrá precargados los usuarios, las tarjetas y los merchants.

La empresa necesita un sistema web backend que gestione las operaciones principales.

Se espera que el usuario pueda iniciar sesión con usuario y password y obtener un token.
Luego debe poder usarse el token para gestionar la tarjeta.
El token se vence a los 5'
Mediante el token el usuario reclama las gift cards (pueden ser varias) y puede consultar el saldo y el detalle de gastos.
Los merchants pueden hacer cargos en las tarjetas que hayan sido reclamadas.
Esos cargos se actualizan en el balance de las tarjetas.

El Cliente es muy exigente en sus estándares de desarrollo y nos pide por contrato un desarrollo usando la técnica de TDD, con una completa cobertura con casos de test. ¡Se ve que entienden del tema! 
Les preocupa mucho el código repetido, el uso de Ifs, los malos nombres y las responsabilidades entre objetos mal distribuidas (*).

(*) comentaron algo como que incluye todo lo visto y corregido hasta ahora, porque se va a evaluar también ¯\_(ツ)_/¯
