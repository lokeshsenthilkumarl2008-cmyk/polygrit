import type { Language } from "../types/types";

export const LANGUAGES: Record<Language, { label: string; ext: string; starter: string }> = {
  java: {
    label: "Java",
    ext: "java",
    starter: `public class Main {
    public static void main(String[] args) {
        System.out.println("Hello from Java");
    }
}
`,
  },
  python: {
    label: "Python",
    ext: "py",
    starter: `def price(base, tax):
    return base + base * tax

print(price(100, 0.18))
`,
  },
  javascript: {
    label: "JavaScript",
    ext: "js",
    starter: `function validate(order) {
  return order.qty > 0 && order.price > 0;
}

console.log(validate({ qty: 2, price: 50 }));
`,
  },
};