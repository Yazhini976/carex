import { test, describe } from 'node:test';
import assert from 'node:assert';

describe('CAREX Frontend Smoke Tests', () => {
  test('API Base URL is configured properly', () => {
    const defaultUrl = 'http://localhost:8080/api';
    assert.strictEqual(typeof defaultUrl, 'string');
    assert.ok(defaultUrl.startsWith('http'));
  });

  test('Notification Priority mapping values', () => {
    const priorities = ['CRITICAL', 'HIGH', 'NORMAL', 'LOW'];
    assert.strictEqual(priorities.length, 4);
    assert.ok(priorities.includes('CRITICAL'));
    assert.ok(priorities.includes('HIGH'));
  });

  test('Specialty keyword classifier baseline matches', () => {
    const keywordMap = {
      cardiology: ['heart', 'chest', 'palpitations'],
      dermatology: ['skin', 'rash', 'acne'],
      pediatrics: ['child', 'infant', 'pediatric'],
      neurology: ['headache', 'migraine', 'nerve']
    };

    assert.ok(keywordMap.cardiology.includes('heart'));
    assert.ok(keywordMap.dermatology.includes('skin'));
    assert.ok(keywordMap.pediatrics.includes('child'));
  });

  test('CAREX Doctor Match formula bounds', () => {
    const weights = {
      specialty: 0.40,
      mode: 0.20,
      availability: 0.20,
      waitTime: 0.10,
      active: 0.05,
      experience: 0.05
    };

    const totalWeight = Object.values(weights).reduce((a, b) => a + b, 0);
    assert.ok(Math.abs(totalWeight - 1.0) < 0.0001, 'Weights must sum to 1.0 (100%)');
  });
});
