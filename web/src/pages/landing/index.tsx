import "./landing.css";
import FaqSection from "./components/sections/FaqSection";
import FeaturesSection from "./components/sections/FeaturesSection";
import FinalCtaSection from "./components/sections/FinalCtaSection";
import Footer from "./components/sections/Footer";
import HeroSection from "./components/sections/HeroSection";
import HeroCursor from "./components/primitives/HeroCursor";
import HowItWorksSection from "./components/sections/HowItWorksSection";
import Navbar from "./components/sections/Navbar";
import ProblemSection from "./components/sections/ProblemSection";
import TechSection from "./components/sections/TechSection";
import TestimonialSection from "./components/sections/TestimonialSection";
import UseCasesSection from "./components/sections/UseCasesSection";
import { useParallaxScroll } from "./hooks/useParallaxScroll";
import { useRevealObserver } from "./hooks/useRevealObserver";

const Landing = () => {
  useRevealObserver();
  useParallaxScroll();

  return (
    <div className="whylog-landing min-h-screen w-full">
      <HeroCursor />
      <Navbar />
      <HeroSection />
      <ProblemSection />
      <FeaturesSection />
      <HowItWorksSection />
      <TechSection />
      <UseCasesSection />
      <TestimonialSection />
      <FaqSection />
      <FinalCtaSection />
      <Footer />
    </div>
  );
};

export default Landing;
