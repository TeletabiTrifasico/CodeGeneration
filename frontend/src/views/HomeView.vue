<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'HomeView'
});
</script>

<template>
  <div class="home">
    <div class="hero">
      <h1>Welcome to Banking App</h1>
      <p>Your secure online banking solution</p>
      <router-link to="/login" class="cta-button">Login</router-link>
    </div>
    <div class="features">
      <div class="feature">
        <h2>Secure Banking</h2>
        <p>Bank with confidence using our state-of-the-art security measures.</p>
      </div>
      <div class="feature">
        <h2>Easy Transfers</h2>
        <p>Transfer money to anyone, anytime with just a few clicks.</p>
      </div>
      <div class="feature">
        <h2>Account Management</h2>
        <p>View and manage all your accounts in one place.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home {
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 30px 20px;
}

.hero {
  text-align: center;
  padding: 80px 20px;
  background-color: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 50px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.hero h1 {
  font-size: 3rem;
  color: #333;
  margin-bottom: 20px;
}

.hero p {
  font-size: 1.3rem;
  color: #666;
  margin-bottom: 40px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.cta-button {
  display: inline-block;
  padding: 15px 40px;
  background-color: #4CAF50;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  font-weight: bold;
  font-size: 1.2rem;
  transition: all 0.3s ease;
}

.cta-button:hover {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  margin: 0 auto;
  max-width: 1400px;
}

.feature {
  padding: 35px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.feature h2 {
  font-size: 1.8rem;
  color: #333;
  margin-bottom: 20px;
}

.feature p {
  color: #666;
  line-height: 1.7;
  font-size: 1.1rem;
}

/* Large desktop */
@media (min-width: 1200px) {
  .hero {
    padding: 100px 20px;
  }

  .hero h1 {
    font-size: 3.5rem;
  }
}

@media (min-width: 992px) and (max-width: 1199px) {
  .features {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (min-width: 768px) and (max-width: 991px) {
  .hero h1 {
    font-size: 2.5rem;
  }

  .hero p {
    font-size: 1.2rem;
  }

  .features {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (min-width: 576px) and (max-width: 767px) {
  .hero {
    padding: 60px 20px;
  }

  .hero h1 {
    font-size: 2.2rem;
  }

  .hero p {
    font-size: 1.1rem;
  }

  .features {
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }

  .feature {
    padding: 25px;
  }

  .feature h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 575px) {
  .hero {
    padding: 40px 15px;
  }

  .hero h1 {
    font-size: 2rem;
  }

  .hero p {
    font-size: 1rem;
  }

  .cta-button {
    padding: 12px 30px;
    font-size: 1.1rem;
  }

  .features {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .feature {
    padding: 25px 20px;
  }

  .feature h2 {
    font-size: 1.4rem;
    margin-bottom: 15px;
  }
}
</style>