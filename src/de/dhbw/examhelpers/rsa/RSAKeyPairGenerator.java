package de.dhbw.examhelpers.rsa;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;

import java.math.BigInteger;

public class RSAKeyPairGenerator extends org.bouncycastle.crypto.generators.RSAKeyPairGenerator {

    private static final BigInteger ONE = BigInteger.valueOf(1);

    private RSAKeyGenerationParameters param;

    public void init(
            KeyGenerationParameters param)
    {
        super.init(param);
        this.param = (RSAKeyGenerationParameters)param;
    }

    public AsymmetricCipherKeyPair generateKeyPair(BigInteger p, BigInteger q)
    {
        BigInteger n, d, e, pSub1, qSub1, phi;

        e = param.getPublicExponent();

        //
        // calculate the modulus
        //
        n = p.multiply(q);

        if (p.compareTo(q) < 0)
        {
            phi = p;
            p = q;
            q = phi;
        }

        pSub1 = p.subtract(ONE);
        qSub1 = q.subtract(ONE);
        phi = pSub1.multiply(qSub1);

        //
        // calculate the private exponent
        //
        d = e.modInverse(phi);

        //
        // calculate the CRT factors
        //
        BigInteger    dP, dQ, qInv;

        dP = d.remainder(pSub1);
        dQ = d.remainder(qSub1);
        qInv = q.modInverse(p);

        return new AsymmetricCipherKeyPair(
                new RSAKeyParameters(false, n, e),
                new RSAPrivateCrtKeyParameters(n, e, d, p, q, dP, dQ, qInv));
    }

    public AsymmetricCipherKeyPair generateKeyPair(BigInteger p)
    {
        BigInteger    q, n, d, e, pSub1, qSub1, phi;

        //
        // p and q values should have a length of half the strength in bits
        //
        int strength = param.getStrength();
        int pbitlength = (strength + 1) / 2;
        int qbitlength = strength - pbitlength;
        int mindiffbits = strength / 3;

        e = param.getPublicExponent();

        //
        // generate a modulus of the required length
        //
        for (;;)
        {
            // generate q, prime and (q-1) relatively prime to e,
            // and not equal to p
            //
            for (;;)
            {
                q = new BigInteger(qbitlength, 1, param.getRandom());

                if (q.subtract(p).abs().bitLength() < mindiffbits)
                {
                    continue;
                }

                if (q.mod(e).equals(ONE))
                {
                    continue;
                }

                if (!q.isProbablePrime(param.getCertainty()))
                {
                    continue;
                }

                if (e.gcd(q.subtract(ONE)).equals(ONE))
                {
                    break;
                }
            }

            //
            // calculate the modulus
            //
            n = p.multiply(q);

            if (n.bitLength() == param.getStrength())
            {
                break;
            }

            //
            // if we get here our primes aren't big enough, make the largest
            // of the two p and try again
            //
            p = p.max(q);
        }

        if (p.compareTo(q) < 0)
        {
            phi = p;
            p = q;
            q = phi;
        }

        pSub1 = p.subtract(ONE);
        qSub1 = q.subtract(ONE);
        phi = pSub1.multiply(qSub1);

        //
        // calculate the private exponent
        //
        d = e.modInverse(phi);

        //
        // calculate the CRT factors
        //
        BigInteger    dP, dQ, qInv;

        dP = d.remainder(pSub1);
        dQ = d.remainder(qSub1);
        qInv = q.modInverse(p);

        return new AsymmetricCipherKeyPair(
                new RSAKeyParameters(false, n, e),
                new RSAPrivateCrtKeyParameters(n, e, d, p, q, dP, dQ, qInv));
    }
}
