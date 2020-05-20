#!/bin/sh

if test -f /var/run/secrets/nais.io/serviceuser/password
then
    export  SOKNADSMOTTAKER_PASSWORD=$(cat /var/run/secrets/nais.io/serviceuser/password)
fi
