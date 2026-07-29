import imgBackground from "@/assets/images/background.webp";

export const AuthBackground = () => {
  return (
    <div className="absolute inset-0">
      <div
        className="absolute inset-0"
        style={{
          background:
            "linear-gradient(-15.8deg, rgb(208, 240, 244) 10.78%, rgb(232, 228, 248) 42.16%, rgb(216, 228, 252) 89.22%)",
        }}
      />
      <img
        alt=""
        aria-hidden="true"
        src={imgBackground}
        className="absolute inset-0 h-full w-full object-cover opacity-50"
      />
    </div>
  );
};
